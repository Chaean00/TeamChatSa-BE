package com.chaean.teamchatsa.domain.team.service;

import com.chaean.teamchatsa.domain.team.dto.request.TeamCreateReq;
import com.chaean.teamchatsa.domain.team.dto.request.TeamJoinReq;
import com.chaean.teamchatsa.domain.team.dto.response.TeamDetailRes;
import com.chaean.teamchatsa.domain.team.dto.response.TeamListRes;
import com.chaean.teamchatsa.domain.team.dto.response.TeamMemberRes;
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

import java.util.List;

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
		// 이미 가입한 팀이 있는지 체크
		if (teamMemberRepo.existsByUserIdAndIsDeletedFalse(userId)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 가입한 팀이 존재합니다.");
		}
		// 중복 팀명 체크
		if (teamRepo.existsByNameAndIsDeletedFalse(req.getName())) {
			throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 팀명입니다.");
		}

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
		// 이미 가입한 팀이 있는지 체크
		boolean alreadyMember = teamMemberRepo.existsByUserIdAndIsDeletedFalse(userId);
		if (alreadyMember) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 가입한 팀이 존재합니다.");
		}

		// 이미 가입 신청한 상태인지 체크
		boolean exists = teamJoinRequestRepo.existsByTeamIdAndUserId(teamId, userId);
		if (exists) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 해당 팀에 가입 신청을 한 상태입니다.");
		}

		// 존재하는 팀인지 체크
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

	@Transactional(readOnly = true)
	@Loggable
	public List<TeamMemberRes> findTeamMembers(Long teamId) {
		// 존재하는 팀인지 체크
		boolean existsTeam = teamRepo.existsByIdAndIsDeletedFalse(teamId);
		if (!existsTeam) {
			throw new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 팀입니다.");
		}

		return teamMemberRepo.findTeamMembersByTeamId(teamId);
	}

	@Transactional
	public void changeMemberRole(Long teamId, Long userId, TeamRole newRole) {
		TeamMember teamMember = teamMemberRepo.findByTeamIdAndUserIdAndIsDeletedFalse(teamId, userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 팀원입니다."));

		if (teamMember.getRole() == newRole) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 동일한 역할이 설정되어 있습니다.");
		}

		teamMember.updateRole(newRole);
	}
}
