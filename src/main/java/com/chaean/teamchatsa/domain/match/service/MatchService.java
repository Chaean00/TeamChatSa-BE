package com.chaean.teamchatsa.domain.match.service;

import com.chaean.teamchatsa.domain.match.dto.request.MatchApplicationReq;
import com.chaean.teamchatsa.domain.match.dto.request.MatchPostCreateReq;
import com.chaean.teamchatsa.domain.match.dto.request.MatchPostSearchReq;
import com.chaean.teamchatsa.domain.match.dto.response.MatchApplicantRes;
import com.chaean.teamchatsa.domain.match.dto.response.MatchLocationRes;
import com.chaean.teamchatsa.domain.match.dto.response.MatchPostDetailRes;
import com.chaean.teamchatsa.domain.match.dto.response.MatchPostListRes;
import com.chaean.teamchatsa.global.common.dto.SliceResponse;
import com.chaean.teamchatsa.domain.match.model.*;
import com.chaean.teamchatsa.domain.match.repository.MatchApplicationRepository;
import com.chaean.teamchatsa.domain.match.repository.MatchPostRepository;
import com.chaean.teamchatsa.domain.team.model.Team;
import com.chaean.teamchatsa.domain.team.repository.TeamMemberRepository;
import com.chaean.teamchatsa.domain.team.repository.TeamRepository;
import com.chaean.teamchatsa.global.common.aop.annotation.DistributedLock;
import com.chaean.teamchatsa.global.common.aop.annotation.Loggable;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

	private final MatchPostRepository matchPostRepo;
	private final MatchApplicationRepository matchApplicationRepo;
	private final TeamMemberRepository teamMemberRepo;
	private final TeamRepository teamRepo;

	/** 매치 게시물 등록 */
	@Transactional
	@Loggable
	public void registerMatchPost(Long userId, MatchPostCreateReq req) {
		if (req.getMatchDate().isBefore(LocalDateTime.now())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "매치 날짜는 현재 시각 이후여야 합니다.");
		}

		Long teamId = teamMemberRepo.findTeamIdByUserIdAndIsDeletedFalse(userId);
		if (teamId == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "사용자가 속한 팀이 없습니다.");
		}

		MatchPost matchPost = req.toEntity(teamId);

		matchPostRepo.save(matchPost);
	}

	/** 매치 게시물 삭제 */
	@Transactional
	@Loggable
	public void deleteMatchPost(Long userId, Long matchId) {
		MatchPost matchPost = matchPostRepo.findByIdAndIsDeletedFalse(matchId)
				.orElseThrow(() -> new BusinessException(ErrorCode.MATCH_POST_NOT_FOUND));

		Long teamId = teamMemberRepo.findTeamIdByUserIdAndIsDeletedFalse(userId);
		if (teamId == null || !matchPost.getTeamId().equals(teamId)) {
			throw new BusinessException(ErrorCode.FORBIDDEN, "매치 게시물을 삭제할 권한이 없습니다.");
		}

		boolean existsApplication = matchApplicationRepo.existsByPostIdAndStatus(matchId, MatchApplicationStatus.PENDING);
		if (existsApplication) {
			throw new BusinessException(ErrorCode.INVALID_STATE, "신청이 있는 매치는 삭제할 수 없습니다.");
		}

		matchPost.softDelete();
	}

	/** 매치 게시물 목록 조회 */
	@Transactional(readOnly = true)
	@Loggable
	public Slice<MatchPostListRes> findMatchPostList(MatchPostSearchReq req) {
		// 추후에 동적 정렬 조건 설정 가능
		Sort sort = Sort.by(
				Sort.Order.asc("matchDate"),
				Sort.Order.desc("id")
		);

		Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), sort);

		return matchPostRepo.findMatchPostsWithPagination(req, pageable);
	}

	/** 특정 팀의 매치 게시물 목록 조회 */
	@Transactional(readOnly = true)
	@Loggable
	public Slice<MatchPostListRes> findMatchPostListByTeamId(Long teamId, int page, int size) {
		if (!teamRepo.existsByIdAndIsDeletedFalse(teamId)) {
			throw new BusinessException(ErrorCode.TEAM_NOT_FOUND);
		}

		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending().and(Sort.by("id").descending()));

		return matchPostRepo.findMatchPostsByTeamId(teamId, pageable);
	}

	/** 매치 게시물 상세 조회 */
	@Transactional(readOnly = true)
	@Loggable
	public MatchPostDetailRes findMatchPostDetail(Long matchId) {
		MatchPostDetailRes content = matchPostRepo.findMatchPostDetailById(matchId);
		if (content == null) {
			throw new BusinessException(ErrorCode.MATCH_POST_NOT_FOUND);
		}

		return content;
	}

	/** 매치 신청 */
	@Transactional
	@Loggable
	public void registerMatchApplication(Long userId, Long matchId, MatchApplicationReq req) {
		MatchPost matchPost = matchPostRepo.findByIdAndIsDeletedFalse(matchId)
				.orElseThrow(() -> new BusinessException(ErrorCode.MATCH_POST_NOT_FOUND));

		if (matchPost.getStatus() != MatchPostStatus.OPEN) {
			throw new BusinessException(ErrorCode.INVALID_STATE, "마감된 매치입니다.");
		}

		Long teamId = teamMemberRepo.findTeamIdByUserIdAndIsDeletedFalse(userId);
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

		MatchApplication matchApplication = MatchApplication.builder()
				.postId(matchPost.getId())
				.applicantTeamId(teamId)
				.message(req.getMessage())
				.build();

		try {
			matchApplicationRepo.save(matchApplication);
		} catch (DataIntegrityViolationException e) {
			throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 신청한 매치입니다.");
		}
	}

	/** 매치 신청 취소 */
	@Transactional
	@Loggable
	public void deleteMatchApplication(Long userId, Long matchId) {
		if (!matchPostRepo.existsByIdAndIsDeletedFalse(matchId)) {
			throw new BusinessException(ErrorCode.MATCH_POST_NOT_FOUND);
		}

		Long teamId = teamMemberRepo.findTeamIdByUserIdAndIsDeletedFalse(userId);
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

	/** 매치 신청 수락 */
	@DistributedLock(key = "'match:' + #matchId")
	@Transactional
	@Loggable
	public String acceptMatchApplication(Long matchId, Long applicantId, Long userId) {
		MatchPost matchPost = matchPostRepo.findByIdAndIsDeletedFalse(matchId)
				.orElseThrow(() -> new BusinessException(ErrorCode.MATCH_POST_NOT_FOUND));

		if (matchPost.getMatchDate().isBefore(LocalDateTime.now())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "지난 매치에는 신청을 수락할 수 없습니다.");
		}

		if (matchPost.getStatus() != MatchPostStatus.OPEN) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 마감된 게시물입니다.");
		}

		Long teamId = teamMemberRepo.findTeamIdByUserIdAndIsDeletedFalse(userId);
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

		Team team = teamRepo.findByIdAndIsDeletedFalse(matchApplication.getApplicantTeamId())
				.orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND, "신청한 팀을 찾을 수 없습니다."));

		return team.getName();
	}

	/** 매치 신청 거절 */
	@Transactional
	@Loggable
	public String rejectMatchApplication(Long matchId, Long applicantId, Long userId) {
		MatchPost matchPost = matchPostRepo.findByIdAndIsDeletedFalse(matchId)
				.orElseThrow(() -> new BusinessException(ErrorCode.MATCH_POST_NOT_FOUND));

		Long teamId = teamMemberRepo.findTeamIdByUserIdAndIsDeletedFalse(userId);
		if (!matchPost.getTeamId().equals(teamId)) {
			throw new BusinessException(ErrorCode.FORBIDDEN, "권한이 없습니다.");
		}

		MatchApplication matchApplication = matchApplicationRepo.findById(applicantId)
				.orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND, "매치 신청을 찾을 수 없습니다."));

		matchApplication.updateStatus(MatchApplicationStatus.REJECTED);

		Team team = teamRepo.findByIdAndIsDeletedFalse(matchApplication.getApplicantTeamId())
				.orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND, "신청한 팀을 찾을 수 없습니다."));

		return team.getName();
	}

	@Transactional(readOnly = true)
	@Loggable
	public List<MatchApplicantRes> getMatchApplicants(Long userId, Long matchId) {
		MatchPost matchPost = matchPostRepo.findByIdAndIsDeletedFalse(matchId)
				.orElseThrow(() -> new BusinessException(ErrorCode.MATCH_POST_NOT_FOUND));

		Long teamId = teamMemberRepo.findTeamIdByUserIdAndIsDeletedFalse(userId);
		if (teamId == null || !matchPost.getTeamId().equals(teamId)) {
			throw new BusinessException(ErrorCode.FORBIDDEN, "매치 신청 목록을 조회할 권한이 없습니다.");
		}

		return matchApplicationRepo.findApplicantsByMatchIdWithTeamInfo(matchId);
	}

	/** 위치 기반 매치 검색 */
	@Transactional(readOnly = true)
	@Loggable
	public SliceResponse<MatchLocationRes> searchMatchesByLocation(Double lat, Double lng, Double radius, int page, int size) {
		// 입력값 검증
		if (lat < -90.0 || lat > 90.0) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "위도는 -90도에서 90도 사이여야 합니다.");
		}
		if (lng < -180.0 || lng > 180.0) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "경도는 -180도에서 180도 사이여야 합니다.");
		}
		if (radius <= 0) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "반경은 0보다 커야 합니다.");
		}

		Pageable pageable = PageRequest.of(page, size);
		Page<MatchLocationRes> result = matchPostRepo.findMatchPostsByLocation(lat, lng, radius, LocalDateTime.now(), pageable)
				.map(projection -> new MatchLocationRes(
						projection.getId(),
						projection.getTitle(),
						projection.getPlaceName(),
						projection.getMatchDate(),
						projection.getTeamName(),
						projection.getAddress(),
						MatchPostStatus.valueOf(projection.getStatus()),
						projection.getLevel(),
						projection.getLat(),
						projection.getLng(),
						projection.getDistance()
				));

		return SliceResponse.from(result);
	}
}
