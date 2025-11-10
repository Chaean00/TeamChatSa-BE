package com.chaean.teamchatsa.domain.team.controller;

import com.chaean.teamchatsa.domain.team.dto.request.TeamCreateReq;
import com.chaean.teamchatsa.domain.team.dto.request.TeamJoinReq;
import com.chaean.teamchatsa.domain.team.dto.response.TeamDetailRes;
import com.chaean.teamchatsa.domain.team.dto.response.TeamListRes;
import com.chaean.teamchatsa.domain.team.service.TeamService;
import com.chaean.teamchatsa.global.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teams")
public class TeamController {

	private final TeamService teamService;

	@PostMapping("")
	@PreAuthorize("hasAuthority('ROLE_PLAYER')")
	public ResponseEntity<ApiResponse<Void>> createTeam(@AuthenticationPrincipal Long userId, @RequestBody @Validated TeamCreateReq req) {
		teamService.registerTeam(userId, req);

		return ResponseEntity.status(HttpStatus.CREATED).body( ApiResponse.success(null));
	}

	@GetMapping("")
	public ResponseEntity<ApiResponse<Slice<TeamListRes>>> getTeamList(
			@PageableDefault(
					size = 20,
					sort = "createdAt",
					direction = Sort.Direction.DESC) Pageable pageable
	) {

		Slice<TeamListRes> res = teamService.findTeamList(pageable);
		return ResponseEntity.ok(ApiResponse.success(res));
	}

	@GetMapping("/{teamId}")
	public ResponseEntity<ApiResponse<TeamDetailRes>> getTeamDetail(@PathVariable Long teamId) {
		return ResponseEntity.ok(ApiResponse.success(teamService.findTeamDetail(teamId)));
	}

	@PostMapping("/{teamId}/join")
	public ResponseEntity<ApiResponse<Void>> joinTeam(
			@PathVariable Long teamId,
			@AuthenticationPrincipal Long userId,
			@RequestBody TeamJoinReq req) {
		teamService.applyToTeam(teamId, userId, req);
		return ResponseEntity.status(HttpStatus.CREATED).body( ApiResponse.success("팀 가입 신청이 완료되었습니다.", null));
	}
}
