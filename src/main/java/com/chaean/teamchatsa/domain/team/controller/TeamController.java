package com.chaean.teamchatsa.domain.team.controller;

import com.chaean.teamchatsa.domain.team.dto.request.TeamCreateReq;
import com.chaean.teamchatsa.domain.team.dto.request.TeamJoinReq;
import com.chaean.teamchatsa.domain.team.dto.response.TeamDetailRes;
import com.chaean.teamchatsa.domain.team.dto.response.TeamListRes;
import com.chaean.teamchatsa.domain.team.model.TeamRole;
import com.chaean.teamchatsa.domain.team.service.TeamService;
import com.chaean.teamchatsa.global.common.aop.annotation.RequireTeamRole;
import com.chaean.teamchatsa.global.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teams")
public class TeamController {

	private final TeamService teamService;

	/** 팀 생성 */
	@PostMapping("")
	public ResponseEntity<ApiResponse<Void>> createTeam(@AuthenticationPrincipal Long userId, @RequestBody @Validated TeamCreateReq req) {
		teamService.registerTeam(userId, req);

		return ResponseEntity.status(HttpStatus.CREATED).body( ApiResponse.success(null));
	}

	/** 팀 목록 조회 (무한 스크롤) */
	@GetMapping("")
	public ResponseEntity<ApiResponse<Slice<TeamListRes>>> getTeamList(
			@PageableDefault(size = 20) Pageable pageable
	) {
		Slice<TeamListRes> res = teamService.findTeamList(pageable);
		return ResponseEntity.ok(ApiResponse.success(res));
	}

	/** 팀 상세 조회 */
	@GetMapping("/{teamId}")
	public ResponseEntity<ApiResponse<TeamDetailRes>> getTeamDetail(@PathVariable Long teamId) {
		return ResponseEntity.ok(ApiResponse.success(teamService.findTeamDetail(teamId)));
	}

	/** 팀 가입 신청 */
	@PostMapping("/{teamId}/join")
	public ResponseEntity<ApiResponse<Void>> joinTeam(
			@PathVariable Long teamId,
			@AuthenticationPrincipal Long userId,
			@RequestBody TeamJoinReq req) {
		teamService.applyToTeam(teamId, userId, req);
		return ResponseEntity.status(HttpStatus.CREATED).body( ApiResponse.success("팀 가입 신청이 완료되었습니다.", null));
	}

	/** 팀 삭제 - LEADER만 가능 */
	@DeleteMapping("/{teamId}")
	@RequireTeamRole(TeamRole.LEADER)
	public ResponseEntity<ApiResponse<Void>> deleteTeam(@PathVariable Long teamId) {
		teamService.deleteTeam(teamId);
		return ResponseEntity.ok(ApiResponse.success("팀이 삭제되었습니다.", null));
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
