package com.chaean.teamchatsa.domain.match.service;

import com.chaean.teamchatsa.domain.match.dto.request.MatchApplicationRequest;
import com.chaean.teamchatsa.domain.match.dto.request.MatchMapSearchRequest;
import com.chaean.teamchatsa.domain.match.dto.request.MatchPostCreateRequest;
import com.chaean.teamchatsa.domain.match.dto.request.MatchPostSearchRequest;
import com.chaean.teamchatsa.domain.match.dto.response.MatchApplicantResponse;
import com.chaean.teamchatsa.domain.match.dto.response.MatchMapResponse;
import com.chaean.teamchatsa.domain.match.dto.response.MatchPostDetailResponse;
import com.chaean.teamchatsa.domain.match.dto.response.MatchPostListResponse;
import com.chaean.teamchatsa.domain.match.dto.response.MyMatchHistoryResponse;
import com.chaean.teamchatsa.domain.match.event.MatchApplicationCreatedEvent;
import com.chaean.teamchatsa.domain.match.event.MatchApplicationProcessedEvent;
import com.chaean.teamchatsa.domain.match.model.MatchApplication;
import com.chaean.teamchatsa.domain.match.model.MatchApplicationStatus;
import com.chaean.teamchatsa.domain.match.model.MatchPost;
import com.chaean.teamchatsa.domain.match.model.MatchPostStatus;
import com.chaean.teamchatsa.domain.match.model.MatchResult;
import com.chaean.teamchatsa.domain.match.repository.MatchApplicationRepository;
import com.chaean.teamchatsa.domain.match.repository.MatchPostRepository;
import com.chaean.teamchatsa.domain.match.repository.MatchResultRepository;
import com.chaean.teamchatsa.domain.match.repository.projection.MatchLocationProjection;
import com.chaean.teamchatsa.domain.team.model.Team;
import com.chaean.teamchatsa.domain.team.model.TeamMember;
import com.chaean.teamchatsa.domain.team.model.TeamRole;
import com.chaean.teamchatsa.domain.team.repository.TeamMemberRepository;
import com.chaean.teamchatsa.domain.team.repository.TeamRepository;
import com.chaean.teamchatsa.domain.team.repository.TeamReviewRepository;
import com.chaean.teamchatsa.global.common.aop.annotation.DistributedLock;
import com.chaean.teamchatsa.global.common.aop.annotation.Loggable;
import com.chaean.teamchatsa.global.common.dto.SliceResponse;
import com.chaean.teamchatsa.global.common.util.CacheKeyGenerator;
import com.chaean.teamchatsa.global.common.util.RedisCacheUtil;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

	private static final Duration CACHE_TTL = Duration.ofMinutes(5);
	private static final int MAP_MARKER_LIMIT = 40;
	private static final int WIDE_ZOOM_THRESHOLD = 4;
	private static final double FOCUSED_BBOX_SCALE = 0.4;
	private final MatchPostRepository matchPostRepo;
	private final MatchApplicationRepository matchApplicationRepo;
	private final MatchResultRepository matchResultRepo;
	private final TeamMemberRepository teamMemberRepo;
	private final TeamRepository teamRepo;
	private final TeamReviewRepository teamReviewRepo;
	private final ApplicationEventPublisher eventPublisher;
	private final RedisCacheUtil cacheUtil;
	private final CacheKeyGenerator cacheKeyGen;

	/**
	 * 매치 게시물 등록
	 */
	@Transactional
	@Loggable
	public void registerMatchPost(Long userId, MatchPostCreateRequest req) {
		if (req.getMatchDate().isBefore(LocalDateTime.now())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "매치 날짜는 현재 시각 이후여야 합니다.");
		}

		Long teamId = teamMemberRepo.findTeamIdByUserId(userId);
		if (teamId == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "사용자가 속한 팀이 없습니다.");
		}

		MatchPost matchPost = MatchPost.create(
				teamId,
				req.getTitle(),
				req.getContent(),
				req.getHeadCount(),
				req.getMatchDate(),
				req.getLat(),
				req.getLng(),
				req.getAddress(),
				req.getPlaceName()
		);
		matchPostRepo.save(matchPost);

		// 매치 목록 캐시 무효화
		deleteMatchPostsCache();
	}

	/**
	 * 매치 게시물 삭제
	 */
	@Transactional
	@Loggable
	public void deleteMatchPost(Long userId, Long matchId) {
		MatchPost matchPost = matchPostRepo.findById(matchId)
				.orElseThrow(() -> new BusinessException(ErrorCode.MATCH_POST_NOT_FOUND));

		Long teamId = teamMemberRepo.findTeamIdByUserId(userId);
		if (teamId == null || !matchPost.getTeamId().equals(teamId)) {
			throw new BusinessException(ErrorCode.FORBIDDEN, "매치 게시물을 삭제할 권한이 없습니다.");
		}

		boolean existsApplication = matchApplicationRepo.existsByPostIdAndStatus(matchId, MatchApplicationStatus.PENDING);
		if (existsApplication) {
			throw new BusinessException(ErrorCode.INVALID_STATE, "신청이 있는 매치는 삭제할 수 없습니다.");
		}

		matchPostRepo.delete(matchPost);

		// 매치 목록 캐시 무효화
		deleteMatchPostsCache();
	}

	/**
	 * 매치 게시물 목록 조회
	 */
	@Transactional(readOnly = true)
	@Loggable
	public SliceResponse<MatchPostListResponse> findMatchPosts(MatchPostSearchRequest req) {
		// 캐싱 대상 확인
		if (!cacheKeyGen.isCacheable(req)) {
			return SliceResponse.from(fetchMatchPostsDatabase(req));
		}

		// 캐시 키 생성
		String cacheKey = cacheKeyGen.generateMatchListKey(req);

		// 캐시 조회
		SliceResponse<MatchPostListResponse> cached = cacheUtil.get(
				cacheKey,
				new TypeReference<SliceResponse<MatchPostListResponse>>() {
				}
		);

		if (cached != null) {
			log.info("Cache HIT: {}", cacheKey);
			return cached;
		}

		// 캐시 미스 → DB 조회
		log.info("Cache MISS: {}", cacheKey);
		Slice<MatchPostListResponse> result = fetchMatchPostsDatabase(req);
		SliceResponse<MatchPostListResponse> response = SliceResponse.from(result);

		// 캐시 저장
		cacheUtil.set(cacheKey, response, CACHE_TTL);

		return response;
	}

	private Slice<MatchPostListResponse> fetchMatchPostsDatabase(MatchPostSearchRequest req) {
		Pageable pageable = PageRequest.of(req.getPage(), req.getSize());
		return matchPostRepo.findMatchPostsWithPagination(req, pageable);
	}

	private void deleteMatchPostsCache() {
		String pattern = cacheKeyGen.getMatchListInvalidationPattern();
		cacheUtil.deleteByPattern(pattern);
	}

	@Transactional(readOnly = true)
	@Loggable
	public List<MyMatchHistoryResponse> findMyMatchHistory(Long userId) {
		TeamMember teamMember = teamMemberRepo.findByUserId(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_TEAM_MEMBER, "소속 팀이 없습니다."));

		Long myTeamId = teamMember.getTeamId();
		boolean canManageResult = teamMember.getRole() == TeamRole.LEADER || teamMember.getRole() == TeamRole.CO_LEADER;

		Map<Long, MatchPost> historyMap = new HashMap<>();
		matchPostRepo.findByTeamIdAndAcceptedApplicationIdIsNotNullOrderByMatchDateDesc(myTeamId)
				.forEach(matchPost -> historyMap.put(matchPost.getId(), matchPost));
		matchPostRepo.findAcceptedMatchesByApplicantTeamId(myTeamId, MatchApplicationStatus.ACCEPTED)
				.forEach(matchPost -> historyMap.put(matchPost.getId(), matchPost));

		List<MatchPost> history = new ArrayList<>(historyMap.values());
		if (history.isEmpty()) {
			return List.of();
		}

		List<Long> acceptedApplicationIds = history.stream()
				.map(MatchPost::getAcceptedApplicationId)
				.filter(id -> id != null)
				.toList();
		Map<Long, MatchApplication> applicationMap = matchApplicationRepo.findAllByIdIn(acceptedApplicationIds)
				.stream()
				.collect(Collectors.toMap(MatchApplication::getId, application -> application));

		List<Long> matchPostIds = history.stream().map(MatchPost::getId).toList();
		Map<Long, MatchResult> resultMap = matchResultRepo.findByMatchPostIdIn(matchPostIds)
				.stream()
				.collect(Collectors.toMap(MatchResult::getMatchPostId, result -> result));

		Set<Long> teamIds = new HashSet<>();
		history.forEach(matchPost -> {
			teamIds.add(matchPost.getTeamId());
			MatchApplication application = applicationMap.get(matchPost.getAcceptedApplicationId());
			if (application != null) {
				teamIds.add(application.getApplicantTeamId());
			}
		});
		Map<Long, Team> teamMap = teamRepo.findAllById(teamIds)
				.stream()
				.collect(Collectors.toMap(Team::getId, team -> team));

		Map<Long, Set<Long>> reviewedTeamsByMatch = teamReviewRepo.findByReviewerUserIdAndMatchIdIn(userId, matchPostIds)
				.stream()
				.collect(Collectors.groupingBy(
						teamReview -> teamReview.getMatchId(),
						Collectors.mapping(teamReview -> teamReview.getTeamId(), Collectors.toSet())
				));

		LocalDateTime now = LocalDateTime.now();

		return history.stream()
				.sorted(Comparator.comparing(MatchPost::getMatchDate).reversed())
				.map(matchPost -> {
					MatchApplication acceptedApplication = applicationMap.get(matchPost.getAcceptedApplicationId());
					if (acceptedApplication == null) {
						return null;
					}

					Long homeTeamId = matchPost.getTeamId();
					Long awayTeamId = acceptedApplication.getApplicantTeamId();
					Long opponentTeamId = myTeamId.equals(homeTeamId) ? awayTeamId : homeTeamId;
					boolean matchCompleted = !matchPost.getMatchDate().plusHours(2).isAfter(now);
					boolean resultRegistered = resultMap.containsKey(matchPost.getId());
					boolean reviewWritten = reviewedTeamsByMatch.getOrDefault(matchPost.getId(), Set.of()).contains(opponentTeamId);
					MatchResult result = resultMap.get(matchPost.getId());

					return MyMatchHistoryResponse.builder()
							.matchPostId(matchPost.getId())
							.matchTitle(matchPost.getTitle())
							.matchDate(matchPost.getMatchDate())
							.placeName(matchPost.getPlaceName())
							.address(matchPost.getAddress())
							.matchPhase(matchCompleted ? "COMPLETED" : "SCHEDULED")
							.homeTeamId(homeTeamId)
							.homeTeamName(teamMap.get(homeTeamId) != null ? teamMap.get(homeTeamId).getName() : null)
							.awayTeamId(awayTeamId)
							.awayTeamName(teamMap.get(awayTeamId) != null ? teamMap.get(awayTeamId).getName() : null)
							.opponentTeamId(opponentTeamId)
							.opponentTeamName(teamMap.get(opponentTeamId) != null ? teamMap.get(opponentTeamId).getName() : null)
							.resultRegistered(resultRegistered)
							.homeScore(result != null ? result.getHomeScore() : null)
							.awayScore(result != null ? result.getAwayScore() : null)
							.reviewWritten(reviewWritten)
							.canRegisterResult(matchCompleted && !resultRegistered && canManageResult)
							.canReview(matchCompleted && !reviewWritten)
							.build();
				})
				.filter(response -> response != null)
				.toList();
	}

	/**
	 * 특정 팀의 매치 게시물 목록 조회
	 */
	@Transactional(readOnly = true)
	@Loggable
	public SliceResponse<MatchPostListResponse> findMatchPostListByTeamId(Long teamId, int page, int size) {
		if (!teamRepo.existsById(teamId)) {
			throw new BusinessException(ErrorCode.TEAM_NOT_FOUND);
		}

		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending().and(Sort.by("id").descending()));

		return SliceResponse.from(matchPostRepo.findMatchPostsByTeamId(teamId, pageable));
	}

	/**
	 * 매치 게시물 상세 조회
	 */
	@Transactional(readOnly = true)
	@Loggable
	public MatchPostDetailResponse findMatchPostDetail(Long matchId, Long userId) {
		// 1. 기본 매치 상세 정보 조회
		MatchPostDetailResponse content = matchPostRepo.findMatchPostDetailById(matchId);
		if (content == null) {
			throw new BusinessException(ErrorCode.MATCH_POST_NOT_FOUND);
		}

		// 2. 비로그인 사용자는 기본 정보만 반환
		if (userId == null) {
			return content;
		}

		// 3. 매치가 확정된 상태인지 확인
		MatchPost matchPost = matchPostRepo.findById(matchId)
				.orElseThrow(() -> new BusinessException(ErrorCode.MATCH_POST_NOT_FOUND));

		if (matchPost.getStatus() != MatchPostStatus.CLOSED || matchPost.getAcceptedApplicationId() == null) {
			return content;
		}

		// 4. 현재 사용자가 이 매치와 관련된 팀 소속인지 확인
		Long myTeamId = teamMemberRepo.findTeamIdByUserId(userId);
		if (myTeamId == null) {
			return content;
		}

		// 5. 확정된 신청 정보를 기준으로 상대 팀 조회
		MatchApplication acceptedApplication = matchApplicationRepo.findById(matchPost.getAcceptedApplicationId())
				.orElse(null);
		if (acceptedApplication == null) {
			return content;
		}

		Long ownerTeamId = matchPost.getTeamId();
		Long opponentTeamId = acceptedApplication.getApplicantTeamId();

		if (!myTeamId.equals(ownerTeamId) && !myTeamId.equals(opponentTeamId)) {
			return content;
		}

		// 6. 양 팀 정보를 조회한 뒤 연락처를 포함한 응답으로 확장
		Team ownerTeam = teamRepo.findById(ownerTeamId).orElse(null);
		Team opponentTeam = teamRepo.findById(opponentTeamId).orElse(null);
		if (ownerTeam == null || opponentTeam == null) {
			return content;
		}

		Team myTeam = myTeamId.equals(ownerTeamId) ? ownerTeam : opponentTeam;
		Team otherTeam = myTeamId.equals(ownerTeamId) ? opponentTeam : ownerTeam;

		return MatchPostDetailResponse.withContactInfo(content, myTeam, otherTeam);
	}

	/**
	 * 매치 신청
	 */
	@Transactional
	@Loggable
	public void registerMatchApplication(Long userId, Long matchId, MatchApplicationRequest req) {
		MatchPost matchPost = matchPostRepo.findById(matchId)
				.orElseThrow(() -> new BusinessException(ErrorCode.MATCH_POST_NOT_FOUND));

		if (matchPost.getStatus() != MatchPostStatus.OPEN) {
			throw new BusinessException(ErrorCode.INVALID_STATE, "마감된 매치입니다.");
		}

		Long teamId = teamMemberRepo.findTeamIdByUserId(userId);
		if (teamId == null) {
			throw new BusinessException(ErrorCode.TEAM_NOT_FOUND, "사용자가 속한 팀이 없습니다.");
		}

		if (matchPost.getTeamId().equals(teamId)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "자신의 팀이 작성한 매치에는 신청할 수 없습니다.");
		}

		boolean alreadyApplied = matchApplicationRepo.existsByPostIdAndApplicantTeamId(matchPost.getId(), teamId);
		if (alreadyApplied) {
			throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 신청한 매치입니다.");
		}

		// 신청 팀 정보 조회
		Team applicantTeam = teamRepo.findById(teamId)
				.orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND, "팀을 찾을 수 없습니다."));

		MatchApplication matchApplication = MatchApplication.create(matchPost.getId(), teamId, req.getMessage());

		try {
			matchApplicationRepo.save(matchApplication);

			// 매치 신청 이벤트 발행
			eventPublisher.publishEvent(new MatchApplicationCreatedEvent(
					matchId,
					matchPost.getTeamId(),  // 게시물 작성 팀 ID
					teamId,  // 신청 팀 ID
					applicantTeam.getName(),
					LocalDateTime.now()
			));

			log.info("매치 신청 이벤트 발행: matchId={}, postOwnerTeamId={}, applicantTeamId={}",
					matchId, matchPost.getTeamId(), teamId);
		} catch (DataIntegrityViolationException e) {
			throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 신청한 매치입니다.");
		}
	}

	/**
	 * 매치 신청 취소
	 */
	@Transactional
	@Loggable
	public void deleteMatchApplication(Long userId, Long matchId) {
		if (!matchPostRepo.existsById(matchId)) {
			throw new BusinessException(ErrorCode.MATCH_POST_NOT_FOUND);
		}

		Long teamId = teamMemberRepo.findTeamIdByUserId(userId);
		if (teamId == null) {
			throw new BusinessException(ErrorCode.TEAM_NOT_FOUND, "사용자가 속한 팀이 없습니다.");
		}

		MatchApplication matchApplication = matchApplicationRepo
				.findByPostIdAndApplicantTeamId(matchId, teamId)
				.orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND, "매치 신청을 찾을 수 없습니다."));

		if (matchApplication.getStatus() != MatchApplicationStatus.PENDING) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "대기 중인 신청만 취소할 수 있습니다.");
		}

		matchApplication.updateStatus(MatchApplicationStatus.CANCELLED);
	}

	/**
	 * 매치 신청 수락
	 */
	@DistributedLock(key = "'match:' + #matchId")
	@Transactional
	@Loggable
	public String acceptMatchApplication(Long matchId, Long applicantId, Long userId) {
		MatchPost matchPost = matchPostRepo.findById(matchId)
				.orElseThrow(() -> new BusinessException(ErrorCode.MATCH_POST_NOT_FOUND));

		if (matchPost.getMatchDate().isBefore(LocalDateTime.now())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "지난 매치에는 신청을 수락할 수 없습니다.");
		}

		if (matchPost.getStatus() != MatchPostStatus.OPEN) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 마감된 게시물입니다.");
		}

		Long teamId = teamMemberRepo.findTeamIdByUserId(userId);
		if (!matchPost.getTeamId().equals(teamId)) {
			throw new BusinessException(ErrorCode.FORBIDDEN, "해당 매치의 팀 멤버가 아닙니다.");
		}

		MatchApplication matchApplication = matchApplicationRepo.findById(applicantId)
				.orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND, "매치 신청을 찾을 수 없습니다."));

		if (matchApplication.getStatus() != MatchApplicationStatus.PENDING) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 처리된 신청입니다.");
		}

		matchApplication.updateStatus(MatchApplicationStatus.ACCEPTED);
		matchPost.updateStatus(MatchPostStatus.CLOSED);
		matchPost.updateOpponent(applicantId);

		List<MatchApplication> pendingApplications = matchApplicationRepo.findAllByPostIdAndStatus(
				matchPost.getId(), MatchApplicationStatus.PENDING);

		pendingApplications.forEach(app -> {
			if (!app.getId().equals(applicantId)) {
				app.updateStatus(MatchApplicationStatus.REJECTED);
			}
		});

		Team team = teamRepo.findById(matchApplication.getApplicantTeamId())
				.orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND, "신청한 팀을 찾을 수 없습니다."));

		// 매치 신청 승인 이벤트 발행
		eventPublisher.publishEvent(new MatchApplicationProcessedEvent(
				matchId,
				matchApplication.getApplicantTeamId(),
				matchPost.getTitle(),
				MatchApplicationStatus.ACCEPTED,
				LocalDateTime.now()
		));

		log.info("매치 신청 승인 이벤트 발행: matchId={}, applicantTeamId={}, status=ACCEPTED",
				matchId, matchApplication.getApplicantTeamId());

		// 매치 목록 캐시 무효화 (상태가 CLOSED로 변경됨)
		deleteMatchPostsCache();

		return team.getName();
	}

	/**
	 * 매치 신청 거절
	 */
	@Transactional
	@Loggable
	public String rejectMatchApplication(Long matchId, Long applicantId, Long userId) {
		MatchPost matchPost = matchPostRepo.findById(matchId)
				.orElseThrow(() -> new BusinessException(ErrorCode.MATCH_POST_NOT_FOUND));

		Long teamId = teamMemberRepo.findTeamIdByUserId(userId);
		if (!matchPost.getTeamId().equals(teamId)) {
			throw new BusinessException(ErrorCode.FORBIDDEN, "권한이 없습니다.");
		}

		MatchApplication matchApplication = matchApplicationRepo.findById(applicantId)
				.orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND, "매치 신청을 찾을 수 없습니다."));

		matchApplication.updateStatus(MatchApplicationStatus.REJECTED);

		Team team = teamRepo.findById(matchApplication.getApplicantTeamId())
				.orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND, "신청한 팀을 찾을 수 없습니다."));

		// 매치 신청 거절 이벤트 발행
		eventPublisher.publishEvent(new MatchApplicationProcessedEvent(
				matchId,
				matchApplication.getApplicantTeamId(),
				matchPost.getTitle(),
				MatchApplicationStatus.REJECTED,
				LocalDateTime.now()
		));

		log.info("매치 신청 거절 이벤트 발행: matchId={}, applicantTeamId={}, status=REJECTED",
				matchId, matchApplication.getApplicantTeamId());

		return team.getName();
	}

	@Transactional(readOnly = true)
	@Loggable
	public List<MatchApplicantResponse> getMatchApplicants(Long userId, Long matchId) {
		MatchPost matchPost = matchPostRepo.findById(matchId)
				.orElseThrow(() -> new BusinessException(ErrorCode.MATCH_POST_NOT_FOUND));

		Long teamId = teamMemberRepo.findTeamIdByUserId(userId);
		if (teamId == null || !matchPost.getTeamId().equals(teamId)) {
			throw new BusinessException(ErrorCode.FORBIDDEN, "매치 신청 목록을 조회할 권한이 없습니다.");
		}

		return matchApplicationRepo.findApplicantsByMatchIdWithTeamInfo(matchId);
	}

	/**
	 * 위치 기반 매치 검색 (Bounding Box + PostGIS)
	 */
	@Transactional(readOnly = true)
	@Loggable
	public List<MatchMapResponse> searchMatchesByLocation(MatchMapSearchRequest req) {
		// 좌표값 검증
		if (req.getSwLat() >= req.getNeLat()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "남서쪽 위도는 북동쪽 위도보다 작아야 합니다.");
		}
		if (req.getSwLng() >= req.getNeLng()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "남서쪽 경도는 북동쪽 경도보다 작아야 합니다.");
		}

		// 중심 좌표 계산
		double centerLat = (req.getSwLat() + req.getNeLat()) / 2.0;
		double centerLng = (req.getSwLng() + req.getNeLng()) / 2.0;
		double querySwLat = req.getSwLat();
		double querySwLng = req.getSwLng();
		double queryNeLat = req.getNeLat();
		double queryNeLng = req.getNeLng();

		if (req.getZoomLevel() > WIDE_ZOOM_THRESHOLD) {
			double latHalfSpan = (req.getNeLat() - req.getSwLat()) * FOCUSED_BBOX_SCALE / 2.0;
			double lngHalfSpan = (req.getNeLng() - req.getSwLng()) * FOCUSED_BBOX_SCALE / 2.0;

			querySwLat = centerLat - latHalfSpan;
			queryNeLat = centerLat + latHalfSpan;
			querySwLng = centerLng - lngHalfSpan;
			queryNeLng = centerLng + lngHalfSpan;
		}

		List<MatchLocationProjection> result = matchPostRepo.findMatchMarkersByLocation(
				querySwLat,
				querySwLng,
				queryNeLat,
				queryNeLng,
				centerLat,
				centerLng,
				LocalDateTime.now(),
				req.getStartDate(),
				req.getEndDate(),
				req.getHeadCount(),
				MAP_MARKER_LIMIT
		);

		return result.stream()
				.map(MatchMapResponse::from)
				.collect(Collectors.toList());
	}

}
