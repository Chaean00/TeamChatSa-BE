package com.chaean.teamchatsa.domain.team.service;

import com.chaean.teamchatsa.domain.team.dto.request.TeamCreateReq;
import com.chaean.teamchatsa.domain.team.dto.request.TeamJoinReq;
import com.chaean.teamchatsa.domain.team.dto.response.TeamApplicationRes;
import com.chaean.teamchatsa.domain.team.dto.response.TeamDetailRes;
import com.chaean.teamchatsa.domain.team.dto.response.TeamListRes;
import com.chaean.teamchatsa.domain.team.dto.response.TeamMemberRes;
import com.chaean.teamchatsa.domain.team.model.*;
import com.chaean.teamchatsa.domain.team.repository.TeamJoinRequestRepository;
import com.chaean.teamchatsa.domain.team.repository.TeamMemberRepository;
import com.chaean.teamchatsa.domain.team.repository.TeamRepository;
import com.chaean.teamchatsa.global.common.aop.annotation.DistributedLock;
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
	public TeamDetailRes findTeamDetail(Long teamId, Long userId) {
		Team team = teamRepo.findByIdAndIsDeletedFalse(teamId)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 팀입니다."));

		Long memberCount = teamMemberRepo.countByTeamIdAndIsDeletedFalse(teamId);

		TeamMember teamMember = teamMemberRepo.findByTeamIdAndUserIdAndIsDeletedFalse(teamId, userId)
				.orElse(null);
		TeamRole userRole = teamMember != null ? teamMember.getRole() : null;

		return TeamDetailRes.fromEntity(team, userRole, memberCount);
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

	/** 팀 가입 신청 목록 조회 */
	@Transactional(readOnly = true)
	@Loggable
	public List<TeamApplicationRes> findTeamApplications(Long teamId, Long userId) {
		// 팀 존재 여부 확인
		if (!teamRepo.existsByIdAndIsDeletedFalse(teamId)) {
			throw new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 팀입니다.");
		}

		// 팀장 또는 부팀장 권한 확인
		TeamMember teamMember = teamMemberRepo.findByTeamIdAndUserIdAndIsDeletedFalse(teamId, userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN, "해당 팀의 멤버가 아닙니다."));

		if (teamMember.getRole() != TeamRole.LEADER && teamMember.getRole() != TeamRole.CO_LEADER) {
			throw new BusinessException(ErrorCode.FORBIDDEN, "팀 가입 신청 목록을 조회할 권한이 없습니다.");
		}

		// PENDING 상태의 가입 신청만 조회
		return teamJoinRequestRepo.findApplicationsByTeamIdAndStatus(teamId, JoinStatus.PENDING);
	}

	/** 팀 가입 신청 수락 */
	@Transactional
	@Loggable
	@DistributedLock(key = "'team:application:accept:' + #applicationId")
	public void acceptTeamApplication(Long teamId, Long applicationId, Long userId) {
		// 팀 존재 여부 확인
		if (!teamRepo.existsByIdAndIsDeletedFalse(teamId)) {
			throw new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 팀입니다.");
		}

		// 팀장 또는 부팀장 권한 확인
		TeamMember teamMember = teamMemberRepo.findByTeamIdAndUserIdAndIsDeletedFalse(teamId, userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN, "해당 팀의 멤버가 아닙니다."));

		if (teamMember.getRole() != TeamRole.LEADER && teamMember.getRole() != TeamRole.CO_LEADER) {
			throw new BusinessException(ErrorCode.FORBIDDEN, "팀 가입 신청을 수락할 권한이 없습니다.");
		}

		// 가입 신청 존재 여부 확인
		TeamApplication application = teamJoinRequestRepo.findByIdAndTeamId(applicationId, teamId)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 가입 신청입니다."));

		// 이미 처리된 신청인지 확인
		if (application.getStatus() != JoinStatus.PENDING) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 처리된 가입 신청입니다.");
		}

		// 신청자가 이미 다른 팀에 가입되어 있는지 확인
		boolean alreadyMember = teamMemberRepo.existsByUserIdAndIsDeletedFalse(application.getUserId());
		if (alreadyMember) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 다른 팀에 가입된 사용자입니다.");
		}

		// 가입 신청 상태 변경
		application.updateStatus(JoinStatus.ACCEPTED);

		// 팀 멤버로 추가
		TeamMember newMember = TeamMember.builder()
				.teamId(teamId)
				.userId(application.getUserId())
				.role(TeamRole.MEMBER)
				.build();

		teamMemberRepo.save(newMember);

		// 해당 사용자의 다른 모든 PENDING 신청을 자동으로 거절 처리
		List<TeamApplication> otherPendingApplications = teamJoinRequestRepo
				.findPendingApplicationsByUserIdExcluding(application.getUserId(), applicationId);

		otherPendingApplications.forEach(app -> app.updateStatus(JoinStatus.REJECTED));
	}

	/** 팀 가입 신청 거절 */
	@Transactional
	@Loggable
	public void rejectTeamApplication(Long teamId, Long applicationId, Long userId) {
		// 팀 존재 여부 확인
		if (!teamRepo.existsByIdAndIsDeletedFalse(teamId)) {
			throw new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 팀입니다.");
		}

		// 팀장 또는 부팀장 권한 확인
		TeamMember teamMember = teamMemberRepo.findByTeamIdAndUserIdAndIsDeletedFalse(teamId, userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN, "해당 팀의 멤버가 아닙니다."));

		if (teamMember.getRole() != TeamRole.LEADER && teamMember.getRole() != TeamRole.CO_LEADER) {
			throw new BusinessException(ErrorCode.FORBIDDEN, "팀 가입 신청을 거절할 권한이 없습니다.");
		}

		// 가입 신청 존재 여부 확인
		TeamApplication application = teamJoinRequestRepo.findByIdAndTeamId(applicationId, teamId)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 가입 신청입니다."));

		// 이미 처리된 신청인지 확인
		if (application.getStatus() != JoinStatus.PENDING) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 처리된 가입 신청입니다.");
		}

		// 가입 신청 상태 변경
		application.updateStatus(JoinStatus.REJECTED);
	}
}
