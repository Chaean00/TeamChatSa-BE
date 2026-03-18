package com.chaean.teamchatsa.domain.team.service;

import com.chaean.teamchatsa.domain.team.dto.request.TeamReviewCreateRequest;
import com.chaean.teamchatsa.domain.team.event.TeamReviewCreatedEvent;
import com.chaean.teamchatsa.domain.team.model.Team;
import com.chaean.teamchatsa.domain.team.model.TeamReview;
import com.chaean.teamchatsa.domain.team.repository.TeamRepository;
import com.chaean.teamchatsa.domain.team.repository.TeamReviewRepository;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamReviewService {

	private final TeamReviewRepository teamReviewRepository;
	private final TeamRepository teamRepository;
	private final ApplicationEventPublisher eventPublisher;

	public void registerReview(Long userId, TeamReviewCreateRequest req) {
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
