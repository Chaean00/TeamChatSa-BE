package com.chaean.teamchatsa.domain.team.controller;

import com.chaean.teamchatsa.domain.team.dto.request.TeamCreateReq;
import com.chaean.teamchatsa.domain.team.dto.response.TeamListRes;
import com.chaean.teamchatsa.domain.team.service.TeamService;
import com.chaean.teamchatsa.global.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teams")
public class TeamController {

	private final TeamService teamService;

	@PostMapping("")
	@PreAuthorize("hasAuthority('ROLE_PLAYER')")
	private ResponseEntity<ApiResponse<Void>> createTeam(@AuthenticationPrincipal Long userId, @RequestBody TeamCreateReq req) {
		teamService.registerTeam(userId, req);

		return ResponseEntity.status(HttpStatus.CREATED).body( ApiResponse.success(null));
	}

	@GetMapping("")
	@PreAuthorize("hasAnyAuthority('ROLE_PLAYER', 'ROLE_LEADER')")
	private ResponseEntity<ApiResponse<TeamListRes>> getTeamList() {
		// TODO : 팀 리스트 조회
		return null;
	}

	@GetMapping("/{teamId}")
	@PreAuthorize("hasAnyAuthority('ROLE_PLAYER', 'ROLE_LEADER')")
	private ResponseEntity<ApiResponse<Void>> getTeamDetail(@PathVariable Long teamId) {
		// TODO : 팀 상세 조회
		return null;
	}

	@PostMapping("/{teamId}/join")
	@PreAuthorize("hasAuthority('ROLE_PLAYER')")
	private ResponseEntity<ApiResponse<Void>> joinTeam(@PathVariable Long teamId) {
		// TODO : 팀 가입 신청
		return null;
	}
}
