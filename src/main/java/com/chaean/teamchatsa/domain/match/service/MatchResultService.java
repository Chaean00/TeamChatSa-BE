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
@Transactional
public class MatchResultService {

	private final MatchResultRepository matchResultRepository;
	private final TeamRepository teamRepository;

	/**
	 * Record a match result and update the win rates of both participating teams.
	 *
	 * Creates and persists a MatchResult from the provided request data and recalculates
	 * the win rate for the home and away teams.
	 *
	 * @param userId the ID of the user performing the registration
	 * @param req    the match result data (match post ID, home/away team IDs and scores)
	 */
	public void registerMatchResult(Long userId, MatchResultCreateRequest req) {
		Long winnerTeamId = determineWinner(req);

		MatchResult result = MatchResult.builder()
				.matchPostId(req.getMatchPostId())
				.homeTeamId(req.getHomeTeamId())
				.awayTeamId(req.getAwayTeamId())
				.homeScore(req.getHomeScore())
				.awayScore(req.getAwayScore())
				.winnerTeamId(winnerTeamId)
				.build();

		matchResultRepository.save(result);

		updateTeamWinRate(req.getHomeTeamId());
		updateTeamWinRate(req.getAwayTeamId());
	}

	/**
	 * Determine the winning team's ID from the provided match scores.
	 *
	 * @param req request containing home and away team IDs and their scores
	 * @return the winning team's ID, or `null` if the match is a draw
	 */
	private Long determineWinner(MatchResultCreateRequest req) {
        if (req.getHomeScore() > req.getAwayScore()) {
            return req.getHomeTeamId();
        }
        if (req.getAwayScore() > req.getHomeScore()) {
            return req.getAwayTeamId();
        }
		return null;
	}

	/**
	 * Recalculate and persist the win rate for the specified team.
	 *
	 * Loads the team by id, computes the win rate as won matches divided by total matches,
	 * and updates the team's win rate when the team has at least one recorded match.
	 *
	 * @param teamId the id of the team whose win rate will be updated
	 * @throws BusinessException if no team exists with the given id (ErrorCode.TEAM_NOT_FOUND)
	 */
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
