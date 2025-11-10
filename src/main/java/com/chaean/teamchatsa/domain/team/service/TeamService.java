package com.chaean.teamchatsa.domain.team.service;

import com.chaean.teamchatsa.domain.team.dto.request.TeamCreateReq;
import com.chaean.teamchatsa.domain.team.dto.request.TeamJoinReq;
import com.chaean.teamchatsa.domain.team.dto.response.TeamDetailRes;
import com.chaean.teamchatsa.domain.team.dto.response.TeamListRes;
import com.chaean.teamchatsa.domain.team.model.Team;
import com.chaean.teamchatsa.domain.team.model.TeamJoinRequest;
import com.chaean.teamchatsa.domain.team.model.TeamMember;
import com.chaean.teamchatsa.domain.team.repository.TeamJoinRequestRepository;
import com.chaean.teamchatsa.domain.team.repository.TeamMemberRepository;
import com.chaean.teamchatsa.domain.team.repository.TeamRepository;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {

	private final TeamRepository teamRepo;
	private final TeamMemberRepository teamMemberRepo;
	private final TeamJoinRequestRepository teamJoinRequestRepo;

	@Transactional
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
		teamMemberRepo.save(TeamMember.builder()
						.teamId(team.getId())
						.userId(userId)
						.build());
	}

	@Transactional(readOnly = true)
	public Slice<TeamListRes> findTeamList(Pageable pageable) {
		return teamRepo.findTeamListSlice(pageable);
	}

	@Transactional(readOnly = true)
	public TeamDetailRes findTeamDetail(Long teamId) {
		Team team = teamRepo.findById(teamId)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 팀입니다."));

		Long memberCount = teamMemberRepo.countByTeamIdAndIsDeletedFalse(teamId);

		return TeamDetailRes.fromEntity(team, memberCount);
	}

	@Transactional
	public void applyToTeam(Long teamId, Long userId, TeamJoinReq req) {
		boolean exists = teamJoinRequestRepo.existsByTeamIdAndUserId(teamId, userId);
		if (exists) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 해당 팀에 가입 신청을 한 상태입니다.");
		}

		Team team = teamRepo.findById(teamId)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 팀입니다."));

		teamJoinRequestRepo.save(TeamJoinRequest.of(teamId, userId, req.message()));
	}
}
