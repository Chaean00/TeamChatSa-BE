package com.chaean.teamchatsa.domain.team.service;

import com.chaean.teamchatsa.domain.team.dto.request.TeamCreateReq;
import com.chaean.teamchatsa.domain.team.dto.request.TeamJoinReq;
import com.chaean.teamchatsa.domain.team.dto.response.TeamDetailRes;
import com.chaean.teamchatsa.domain.team.dto.response.TeamListRes;
import com.chaean.teamchatsa.domain.team.model.Team;
import com.chaean.teamchatsa.domain.team.model.TeamApplication;
import com.chaean.teamchatsa.domain.team.model.TeamMember;
import com.chaean.teamchatsa.domain.team.model.TeamRole;
import com.chaean.teamchatsa.domain.team.repository.TeamJoinRequestRepository;
import com.chaean.teamchatsa.domain.team.repository.TeamMemberRepository;
import com.chaean.teamchatsa.domain.team.repository.TeamRepository;
import com.chaean.teamchatsa.global.common.aop.annotation.Loggable;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
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
	@Loggable
	public void registerTeam(Long userId, TeamCreateReq req) {
		if (teamMemberRepo.existsByUserIdAndIsDeletedFalse(userId)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 가입한 팀이 존재합니다.");
		}
		if (teamRepo.existsByLeaderUserIdAndIsDeletedFalse(userId)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 리더로 존재하는 팀이 존재합니다. 팀은 1개만 생성가능합니다.");
		}

		// TODO : 중복 팀명 체크

		// TODO : of 패턴으로 전환
		Team team = Team.builder()
				.leaderUserId(userId)
				.name(req.getName())
				.area(req.getArea())
				.description(req.getDescription())
				.contactType(req.getContactType())
				.contact(req.getContact())
				.img(req.getImgUrl())
				.level(req.getLevel())
				.build();

		teamRepo.save(team);

		TeamMember teamMember = TeamMember.builder()
				.teamId(team.getId())
				.userId(userId)
				.role(TeamRole.LEADER)
				.build();

		teamMemberRepo.save(teamMember);
	}

	@Transactional(readOnly = true)
	@Loggable
	public Slice<TeamListRes> findTeamList(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		return teamRepo.findTeamListWithPagination(pageable);
	}

	@Transactional(readOnly = true)
	@Loggable
	public TeamDetailRes findTeamDetail(Long teamId) {
		Team team = teamRepo.findByIdAndIsDeletedFalse(teamId)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 팀입니다."));

		Long memberCount = teamMemberRepo.countByTeamIdAndIsDeletedFalse(teamId);

		return TeamDetailRes.fromEntity(team, memberCount);
	}

	@Transactional
	@Loggable
	public void applyToTeam(Long teamId, Long userId, TeamJoinReq req) {
		boolean exists = teamJoinRequestRepo.existsByTeamIdAndUserId(teamId, userId);
		if (exists) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 해당 팀에 가입 신청을 한 상태입니다.");
		}

		boolean existsTeam = teamRepo.existsByIdAndIsDeletedFalse(teamId);
		if (!existsTeam) {
			throw new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 팀입니다.");
		}

		teamJoinRequestRepo.save(TeamApplication.of(teamId, userId, req.getMessage()));
	}

	@Transactional
	@Loggable
	public void deleteTeam(Long teamId) {
		Team team = teamRepo.findByIdAndIsDeletedFalse(teamId)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 팀입니다."));

		Long memberCount = teamMemberRepo.countByTeamIdAndIsDeletedFalse(teamId);

		// 본인 제외
		if (memberCount > 1) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "팀원 존재로 인해 팀을 삭제할 수 없습니다.");
		}

		team.softDelete();
	}
}
