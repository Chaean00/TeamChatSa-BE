package com.chaean.teamchatsa.domain.match.service;

import com.chaean.teamchatsa.domain.match.dto.request.MatchResultCreateRequest;
import com.chaean.teamchatsa.domain.match.model.MatchResult;
import com.chaean.teamchatsa.domain.match.repository.MatchResultRepository;
import com.chaean.teamchatsa.domain.team.model.Team;
import com.chaean.teamchatsa.domain.team.repository.TeamRepository;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MatchResultService {

	private final MatchResultRepository matchResultRepository;
	private final TeamRepository teamRepository;

	@Transactional
	public void registerMatchResult(MatchResultCreateRequest req) {
		// 승리 팀 결정
		Long winnerTeamId = determineWinner(req);

		MatchResult result = MatchResult.create(
				req.getMatchPostId(),
				req.getHomeTeamId(),
				req.getAwayTeamId(),
				req.getHomeScore(),
				req.getAwayScore(),
				winnerTeamId
		);

		// 경기 결과 저장
		matchResultRepository.save(result);

		// 승률 업데이트
		updateTeamWinRate(req.getHomeTeamId());
		updateTeamWinRate(req.getAwayTeamId());
	}

	private Long determineWinner(MatchResultCreateRequest req) {
		if (req.getHomeScore() > req.getAwayScore()) {
			return req.getHomeTeamId();
		}
		if (req.getAwayScore() > req.getHomeScore()) {
			return req.getAwayTeamId();
		}
		return null;
	}

	private void updateTeamWinRate(Long teamId) {
		Team team = teamRepository.findById(teamId)
				.orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

		long totalMatches = matchResultRepository.countTotalMatches(teamId);
		long wonMatches = matchResultRepository.countByWinnerTeamId(teamId);

		if (totalMatches > 0) {
			double winRate = (double) wonMatches / totalMatches;
			team.updateWinRate(winRate);
		}
	}
}
