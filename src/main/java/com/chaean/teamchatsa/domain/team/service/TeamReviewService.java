package com.chaean.teamchatsa.domain.team.service;

import com.chaean.teamchatsa.domain.team.dto.request.TeamReviewCreateRequest;
import com.chaean.teamchatsa.domain.team.event.TeamReviewCreatedEvent;
import com.chaean.teamchatsa.domain.match.model.MatchApplication;
import com.chaean.teamchatsa.domain.match.model.MatchPost;
import com.chaean.teamchatsa.domain.match.repository.MatchApplicationRepository;
import com.chaean.teamchatsa.domain.match.repository.MatchPostRepository;
import com.chaean.teamchatsa.domain.team.model.Team;
import com.chaean.teamchatsa.domain.team.model.TeamReview;
import com.chaean.teamchatsa.domain.team.repository.TeamMemberRepository;
import com.chaean.teamchatsa.domain.team.repository.TeamRepository;
import com.chaean.teamchatsa.domain.team.repository.TeamReviewRepository;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamReviewService {

	private final TeamReviewRepository teamReviewRepository;
	private final TeamMemberRepository teamMemberRepository;
	private final MatchPostRepository matchPostRepository;
	private final MatchApplicationRepository matchApplicationRepository;
	private final TeamRepository teamRepository;
	private final ApplicationEventPublisher eventPublisher;

	public void registerReview(Long userId, TeamReviewCreateRequest req) {
		Long myTeamId = teamMemberRepository.findTeamIdByUserId(userId);
		if (myTeamId == null) {
			throw new BusinessException(ErrorCode.NOT_TEAM_MEMBER, "소속 팀이 없습니다.");
		}

		MatchPost matchPost = matchPostRepository.findById(req.getMatchId())
				.orElseThrow(() -> new BusinessException(ErrorCode.MATCH_POST_NOT_FOUND));
		if (matchPost.getAcceptedApplicationId() == null) {
			throw new BusinessException(ErrorCode.INVALID_STATE, "상대 팀이 확정된 경기만 리뷰를 남길 수 있습니다.");
		}

		MatchApplication acceptedApplication = matchApplicationRepository.findById(matchPost.getAcceptedApplicationId())
				.orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND, "확정된 상대 팀 정보를 찾을 수 없습니다."));

		if (matchPost.getMatchDate().plusHours(2).isAfter(LocalDateTime.now())) {
			throw new BusinessException(ErrorCode.INVALID_STATE, "경기 시작 2시간 이후부터 리뷰를 작성할 수 있습니다.");
		}

		Long homeTeamId = matchPost.getTeamId();
		Long awayTeamId = acceptedApplication.getApplicantTeamId();
		if (!myTeamId.equals(homeTeamId) && !myTeamId.equals(awayTeamId)) {
			throw new BusinessException(ErrorCode.FORBIDDEN, "참여한 경기만 리뷰를 작성할 수 있습니다.");
		}

		Long opponentTeamId = myTeamId.equals(homeTeamId) ? awayTeamId : homeTeamId;
		if (!req.getTeamId().equals(opponentTeamId)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "상대 팀 정보가 올바르지 않습니다.");
		}

		if (teamReviewRepository.existsByTeamIdAndReviewerUserIdAndMatchId(req.getTeamId(), userId, req.getMatchId())) {
			throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 해당 경기에 대한 리뷰를 작성했습니다.");
		}

		Team team = teamRepository.findById(req.getTeamId())
				.orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

		// 팀 리뷰 생성
		TeamReview review = TeamReview.builder()
				.teamId(req.getTeamId())
				.reviewerUserId(userId)
				.matchId(req.getMatchId())
				.rating(req.getRating())
				.content(req.getContent())
				.build();

		teamReviewRepository.save(review);

		// 임베딩을 위한 팀 리뷰 생성 이벤트 발행
		eventPublisher.publishEvent(new TeamReviewCreatedEvent(
				team.getId()
		));
	}
}
