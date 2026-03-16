package com.chaean.teamchatsa.domain.team.service;

import com.chaean.teamchatsa.domain.team.dto.request.TeamReviewCreateRequest;
import com.chaean.teamchatsa.domain.team.model.Team;
import com.chaean.teamchatsa.domain.team.model.TeamReview;
import com.chaean.teamchatsa.domain.team.repository.TeamRepository;
import com.chaean.teamchatsa.domain.team.repository.TeamReviewRepository;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamReviewService {

	private final TeamReviewRepository teamReviewRepository;
	private final TeamRepository teamRepository;

	public void registerReview(Long userId, TeamReviewCreateRequest req) {
		Team team = teamRepository.findById(req.getTeamId())
				.orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

		TeamReview review = TeamReview.builder()
				.teamId(req.getTeamId())
				.reviewerUserId(userId)
				.matchId(req.getMatchId())
				.rating(req.getRating())
				.content(req.getContent())
				.build();

		teamReviewRepository.save(review);

		// TODO: AI 비동기 요약/벡터 갱신 이벤트 발행 로직 추가 예정
	}
}
