package com.chaean.teamchatsa.domain.match.service;

import com.chaean.teamchatsa.domain.match.dto.request.MatchResultCreateRequest;
import com.chaean.teamchatsa.domain.match.model.MatchApplication;
import com.chaean.teamchatsa.domain.match.model.MatchResult;
import com.chaean.teamchatsa.domain.match.repository.MatchApplicationRepository;
import com.chaean.teamchatsa.domain.match.repository.MatchPostRepository;
import com.chaean.teamchatsa.domain.match.repository.MatchResultRepository;
import com.chaean.teamchatsa.domain.team.model.TeamMember;
import com.chaean.teamchatsa.domain.team.model.TeamRole;
import com.chaean.teamchatsa.domain.team.repository.TeamMemberRepository;
import com.chaean.teamchatsa.domain.team.model.Team;
import com.chaean.teamchatsa.domain.team.repository.TeamRepository;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MatchResultService {

	private final MatchResultRepository matchResultRepository;
	private final MatchPostRepository matchPostRepository;
	private final MatchApplicationRepository matchApplicationRepository;
	private final TeamMemberRepository teamMemberRepository;
	private final TeamRepository teamRepository;

	@Transactional
	public void registerMatchResult(Long userId, MatchResultCreateRequest req) {
		TeamMember teamMember = teamMemberRepository.findByUserId(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_TEAM_MEMBER, "소속 팀이 없습니다."));

		if (teamMember.getRole() != TeamRole.LEADER && teamMember.getRole() != TeamRole.CO_LEADER) {
			throw new BusinessException(ErrorCode.INSUFFICIENT_TEAM_ROLE, "경기 결과는 팀 운영진만 등록할 수 있습니다.");
		}

		var matchPost = matchPostRepository.findById(req.getMatchPostId())
				.orElseThrow(() -> new BusinessException(ErrorCode.MATCH_POST_NOT_FOUND));
		if (matchPost.getAcceptedApplicationId() == null) {
			throw new BusinessException(ErrorCode.INVALID_STATE, "상대 팀이 확정된 경기만 결과를 등록할 수 있습니다.");
		}

		MatchApplication acceptedApplication = matchApplicationRepository.findById(matchPost.getAcceptedApplicationId())
				.orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND, "확정된 상대 팀 정보를 찾을 수 없습니다."));

		if (matchPost.getMatchDate().plusHours(2).isAfter(LocalDateTime.now())) {
			throw new BusinessException(ErrorCode.INVALID_STATE, "경기 시작 2시간 이후부터 결과를 등록할 수 있습니다.");
		}

		Long homeTeamId = matchPost.getTeamId();
		Long awayTeamId = acceptedApplication.getApplicantTeamId();
		if (!teamMember.getTeamId().equals(homeTeamId) && !teamMember.getTeamId().equals(awayTeamId)) {
			throw new BusinessException(ErrorCode.FORBIDDEN, "참여한 경기의 결과만 등록할 수 있습니다.");
		}

		if (!homeTeamId.equals(req.getHomeTeamId()) || !awayTeamId.equals(req.getAwayTeamId())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "참여 팀 정보가 올바르지 않습니다.");
		}

		if (matchResultRepository.existsByMatchPostId(req.getMatchPostId())) {
			throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 경기 결과가 등록되었습니다.");
		}

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
