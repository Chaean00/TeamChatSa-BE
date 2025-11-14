package com.chaean.teamchatsa.domain.match.service;

import com.chaean.teamchatsa.domain.match.dto.request.MatchApplicationReq;
import com.chaean.teamchatsa.domain.match.dto.request.MatchPostCreateReq;
import com.chaean.teamchatsa.domain.match.dto.response.MatchPostDetailRes;
import com.chaean.teamchatsa.domain.match.dto.response.MatchPostListRes;
import com.chaean.teamchatsa.domain.match.model.*;
import com.chaean.teamchatsa.domain.match.repository.MatchApplicationRepository;
import com.chaean.teamchatsa.domain.match.repository.MatchPostRepository;
import com.chaean.teamchatsa.domain.team.model.Team;
import com.chaean.teamchatsa.domain.team.repository.TeamMemberRepository;
import com.chaean.teamchatsa.domain.team.repository.TeamRepository;
import com.chaean.teamchatsa.global.common.aop.annotation.DistributedLock;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
	public void registerMatchPost(Long userId, MatchPostCreateReq req) {
		if (req.matchDate().isBefore(LocalDateTime.now())) {
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
	public void deleteMatchPost(Long userId, Long matchId) {
		MatchPost matchPost = matchPostRepo.findByIdAndIsDeletedFalse(matchId)
				.orElseThrow(() -> new BusinessException(ErrorCode.MATCH_POST_NOT_FOUND));

		Long teamId = teamMemberRepo.findTeamIdByUserIdAndIsDeletedFalse(userId);
		if (teamId == null || !matchPost.getTeamId().equals(teamId)) {
			throw new BusinessException(ErrorCode.FORBIDDEN, "매치 게시물을 삭제할 권한이 없습니다.");
		}

		long applicationCount = matchApplicationRepo.countByPostId(matchId);
		if (applicationCount > 0) {
			throw new BusinessException(ErrorCode.INVALID_STATE, "신청이 있는 매치는 삭제할 수 없습니다.");
		}

		matchPostRepo.delete(matchPost);
	}

	/** 매치 게시물 목록 조회 */
	@Transactional(readOnly = true)
	public Slice<MatchPostListRes> findMatchPostList(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending().and(Sort.by("id").descending()));

		return matchPostRepo.findMatchPostListWithPagination(pageable);
	}

	/** 매치 게시물 상세 조회 */
	@Transactional(readOnly = true)
	public MatchPostDetailRes findMatchPostDetail(Long matchId) {
		MatchPostDetailRes content = matchPostRepo.findMatchPostDetailById(matchId);
		if (content == null) {
			throw new BusinessException(ErrorCode.MATCH_POST_NOT_FOUND);
		}

		return content;
	}

	/** 매치 신청 */
	@Transactional
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
}
