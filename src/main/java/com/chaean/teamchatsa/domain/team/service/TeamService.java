package com.chaean.teamchatsa.domain.team.service;

import com.chaean.teamchatsa.domain.team.dto.request.TeamCreateReq;
import com.chaean.teamchatsa.domain.team.model.Team;
import com.chaean.teamchatsa.domain.team.repository.TeamMemberRepository;
import com.chaean.teamchatsa.domain.team.repository.TeamRepository;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {

	private final TeamRepository teamRepo;
	private final TeamMemberRepository teamMemberRepo;

	public void registerTeam(Long userId, TeamCreateReq req) {
		boolean hasTeam = teamMemberRepo.existsByUserIdAndIsDeletedFalse(userId);
		if (hasTeam) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 가입한 팀이 존재합니다.");
		}
		hasTeam = teamRepo.existsByLeaderUserIdAndIsDeletedFalse(userId);
		if (hasTeam) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 리더로 존재하는 팀이 존재합니다. 팀은 1개만 생성가능합니다.");
		}

		Team team = Team.builder()
				.leaderUserId(userId)
				.name(req.name())
				.area(req.area())
				.description(req.description())
				.contactType(req.contactType())
				.contact(req.contact())
				.img(req.imgUrl())
				.build();

		teamRepo.save(team);
	}
}
