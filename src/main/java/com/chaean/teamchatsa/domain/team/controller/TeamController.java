package com.chaean.teamchatsa.domain.team.controller;

import com.chaean.teamchatsa.domain.team.dto.request.TeamCreateReq;
import com.chaean.teamchatsa.domain.team.dto.request.TeamJoinReq;
import com.chaean.teamchatsa.domain.team.dto.response.TeamApplicationRes;
import com.chaean.teamchatsa.domain.team.dto.response.TeamDetailRes;
import com.chaean.teamchatsa.domain.team.dto.response.TeamListRes;
import com.chaean.teamchatsa.domain.team.dto.response.TeamMemberRes;
import com.chaean.teamchatsa.domain.team.model.TeamRole;
import com.chaean.teamchatsa.domain.team.service.TeamService;
import com.chaean.teamchatsa.global.common.aop.annotation.RequireTeamRole;
import com.chaean.teamchatsa.global.common.dto.ApiResponse;
import com.chaean.teamchatsa.global.common.dto.SliceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "팀 API", description = "팀 생성/조회 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teams")
public class TeamController {

	private final TeamService teamService;

	@Operation(summary = "팀 생성 API", description = "새로운 팀을 생성합니다.")
	@PostMapping("")
	public ResponseEntity<ApiResponse<Void>> createTeam(@AuthenticationPrincipal Long userId, @RequestBody @Validated TeamCreateReq req) {
		teamService.registerTeam(userId, req);

		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
	}

	@Operation(summary = "팀 목록 조회 API", description = "팀 목록을 페이징 처리하여 조회합니다. (무한스크롤)")
	@GetMapping("")
	public ResponseEntity<ApiResponse<SliceResponse<TeamListRes>>> getTeamList(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String teamName
	) {
		SliceResponse<TeamListRes> response = teamService.findTeamList(page, size, teamName);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@Operation(summary = "팀 상세 조회 API", description = "특정 팀의 상세 정보를 조회합니다.")
	@GetMapping("/{teamId}")
	public ResponseEntity<ApiResponse<TeamDetailRes>> getTeamDetail(
			@PathVariable Long teamId,
			@AuthenticationPrincipal Long userId
	) {
		return ResponseEntity.ok(ApiResponse.success(teamService.findTeamDetail(teamId, userId)));
	}

	@Operation(summary = "팀 가입 신청 API", description = "특정 팀에 가입 신청을 합니다.")
	@PostMapping("/{teamId}/join")
	public ResponseEntity<ApiResponse<Void>> joinTeam(
			@PathVariable Long teamId,
			@AuthenticationPrincipal Long userId,
			@RequestBody TeamJoinReq req) {
		teamService.applyToTeam(teamId, userId, req);
		return ResponseEntity.status(HttpStatus.CREATED).body( ApiResponse.success("팀 가입 신청이 완료되었습니다.", null));
	}

	@Operation(summary = "팀 삭제 API", description = "특정 팀을 삭제합니다. (팀장만 가능)")
	@DeleteMapping("/{teamId}")
	@RequireTeamRole(TeamRole.LEADER)
	public ResponseEntity<ApiResponse<Void>> deleteTeam(@PathVariable Long teamId) {
		teamService.deleteTeam(teamId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success("팀이 삭제되었습니다.", null));
	}

	@Operation(summary = "팀원 목록 조회 API", description = "특정 팀의 팀원 목록을 조회합니다.")
	@GetMapping("/{teamId}/members")
	public ResponseEntity<ApiResponse<List<TeamMemberRes>>> getTeamMembers(@PathVariable Long teamId) {
		List<TeamMemberRes> res = teamService.findTeamMembers(teamId);
		return ResponseEntity.ok(ApiResponse.success(res));
	}

	@Operation(summary = "팀원 역할 변경 API", description = "팀원의 역할을 변경합니다. (팀장만 가능)")
	@PatchMapping("/{teamId}/members/{userId}/role")
	@RequireTeamRole(TeamRole.LEADER)
	public ResponseEntity<ApiResponse<Void>> changeMemberRole(
			@PathVariable Long teamId,
			@PathVariable Long userId,
			@RequestParam TeamRole newRole) {
		teamService.changeMemberRole(teamId, userId, newRole);
		return ResponseEntity.ok(ApiResponse.success("팀원 역할이 변경되었습니다.", null));
	}

	@Operation(summary = "팀 가입 신청 목록 조회 API", description = "팀에 들어온 가입 신청 목록을 조회합니다. (팀장/부팀장만 가능)")
	@GetMapping("/{teamId}/applications")
	@RequireTeamRole({TeamRole.LEADER, TeamRole.CO_LEADER})
	public ResponseEntity<ApiResponse<List<TeamApplicationRes>>> getTeamApplications(
			@PathVariable Long teamId,
			@AuthenticationPrincipal Long userId) {
		List<TeamApplicationRes> applications = teamService.findTeamApplications(teamId, userId);
		return ResponseEntity.ok(ApiResponse.success("팀 가입 신청 목록 조회 성공", applications));
	}

	@Operation(summary = "팀 가입 신청 수락 API", description = "팀 가입 신청을 수락하고 팀원으로 추가합니다. (팀장/부팀장만 가능)")
	@PostMapping("/{teamId}/applications/{applicationId}/accept")
	@RequireTeamRole({TeamRole.LEADER, TeamRole.CO_LEADER})
	public ResponseEntity<ApiResponse<Void>> acceptTeamApplication(
			@PathVariable Long teamId,
			@PathVariable Long applicationId,
			@AuthenticationPrincipal Long userId) {
		teamService.acceptTeamApplication(teamId, applicationId, userId);
		return ResponseEntity.ok(ApiResponse.success("팀 가입 신청이 수락되었습니다.", null));
	}

	@Operation(summary = "팀 가입 신청 거절 API", description = "팀 가입 신청을 거절합니다. (팀장/부팀장만 가능)")
	@PostMapping("/{teamId}/applications/{applicationId}/reject")
	@RequireTeamRole({TeamRole.LEADER, TeamRole.CO_LEADER})
	public ResponseEntity<ApiResponse<Void>> rejectTeamApplication(
			@PathVariable Long teamId,
			@PathVariable Long applicationId,
			@AuthenticationPrincipal Long userId) {
		teamService.rejectTeamApplication(teamId, applicationId, userId);
		return ResponseEntity.ok(ApiResponse.success("팀 가입 신청이 거절되었습니다.", null));
	}

	/** 팀 정보 수정 - LEADER 또는 CO_LEADER만 가능
	@PutMapping("/{teamId}")
	@RequireTeamRole(TeamRole.LEADER)
	public ResponseEntity<ApiResponse<Void>> updateTeam(
			@PathVariable Long teamId,
			@RequestBody @Validated TeamCreateReq req) {
		// teamService.updateTeam(teamId, req);
		return ResponseEntity.ok(ApiResponse.success("팀 정보가 수정되었습니다.", null));
	}
	*/

	/** 팀원 강제 퇴출 - LEADER 또는 CO_LEADER만 가능
	@DeleteMapping("/{teamId}/members/{memberId}")
	@RequireTeamRole({TeamRole.LEADER, TeamRole.CO_LEADER})
	public ResponseEntity<ApiResponse<Void>> kickMember(
			@PathVariable Long teamId,
			@PathVariable Long memberId) {
		// teamService.kickMember(teamId, memberId);
		return ResponseEntity.ok(ApiResponse.success("팀원이 강제 퇴출되었습니다.", null));
	}
	*/
}
