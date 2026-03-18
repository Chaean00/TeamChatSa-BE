package com.chaean.teamchatsa.domain.team.controller;

import com.chaean.teamchatsa.domain.team.dto.request.TeamCreateRequest;
import com.chaean.teamchatsa.domain.team.dto.request.TeamJoinRequest;
import com.chaean.teamchatsa.domain.team.dto.request.TeamReviewCreateRequest;
import com.chaean.teamchatsa.domain.team.dto.response.TeamApplicationResponse;
import com.chaean.teamchatsa.domain.team.dto.response.TeamDetailResponse;
import com.chaean.teamchatsa.domain.team.dto.response.TeamListResponse;
import com.chaean.teamchatsa.domain.team.dto.response.TeamMemberResponse;
import com.chaean.teamchatsa.domain.team.model.TeamRole;
import com.chaean.teamchatsa.domain.team.service.TeamReviewService;
import com.chaean.teamchatsa.domain.team.service.TeamService;
import com.chaean.teamchatsa.global.common.aop.annotation.RequireTeamRole;
import com.chaean.teamchatsa.global.common.dto.ApiResponse;
import com.chaean.teamchatsa.global.common.dto.SliceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "팀 API", description = "팀 생성/조회 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teams")
public class TeamController {

	private final TeamService teamService;
	private final TeamReviewService teamReviewService;

	@Operation(summary = "팀 생성 API", description = "새로운 팀을 생성합니다.")
	@PostMapping("")
	public ResponseEntity<ApiResponse<Void>> createTeam(
			@AuthenticationPrincipal Long userId,
			@RequestBody @Validated TeamCreateRequest req
	) {
		teamService.registerTeam(userId, req);

		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
	}

	@Operation(summary = "팀 리뷰 등록 API", description = "경기가 끝난 상대 팀에 대한 리뷰를 등록합니다.")
	@PostMapping("/reviews")
	public ResponseEntity<ApiResponse<Void>> createReview(
			@AuthenticationPrincipal Long userId,
			@RequestBody @Validated TeamReviewCreateRequest req
	) {
		teamReviewService.registerReview(userId, req);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
	}

	@Operation(summary = "팀 목록 조회 API", description = "팀 목록을 페이징 처리하여 조회합니다. (무한스크롤)")
	@GetMapping("")
	public ResponseEntity<ApiResponse<SliceResponse<TeamListResponse>>> getTeams(
			@RequestParam(required = false) String area,
			@RequestParam(required = false) Integer level,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String teamName
	) {
		SliceResponse<TeamListResponse> response = teamService.findTeamList(page, size, teamName, level);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@Operation(summary = "팀 상세 조회 API", description = "팀 상세 정보를 조회합니다.")
	@GetMapping("/{teamId}")
	public ResponseEntity<ApiResponse<TeamDetailResponse>> getTeamDetail(
			@PathVariable Long teamId,
			@AuthenticationPrincipal Long userId
	) {
		TeamDetailResponse teamDetail = teamService.findTeamDetail(teamId, userId);

		return ResponseEntity.ok(ApiResponse.success(teamDetail));
	}

	@Operation(summary = "팀 가입 신청 API", description = "팀에 가입 신청을 합니다.")
	@PostMapping("/{teamId}/applications")
	public ResponseEntity<ApiResponse<Void>> applyToTeam(
			@AuthenticationPrincipal Long userId,
			@PathVariable Long teamId,
			@RequestBody @Validated TeamJoinRequest req) {
		teamService.applyToTeam(teamId, userId, req);

		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
	}

	@Operation(summary = "팀 가입 신청 목록 조회 API", description = "팀 가입 신청 목록을 조회합니다.")
	@RequireTeamRole(TeamRole.LEADER)
	@GetMapping("/{teamId}/applications")
	public ResponseEntity<ApiResponse<List<TeamApplicationResponse>>> getTeamApplications(
			@PathVariable Long teamId,
			@AuthenticationPrincipal Long userId
	) {
		List<TeamApplicationResponse> applications = teamService.findTeamApplications(teamId, userId);

		return ResponseEntity.ok(ApiResponse.success(applications));
	}

	@Operation(summary = "팀 가입 신청 수락 API", description = "팀 가입 신청을 수락합니다.")
	@RequireTeamRole(TeamRole.LEADER)
	@PatchMapping("/{teamId}/applications/{applicationId}/accept")
	public ResponseEntity<ApiResponse<Void>> acceptTeamApplication(
			@PathVariable Long teamId,
			@PathVariable Long applicationId,
			@AuthenticationPrincipal Long userId
	) {
		teamService.acceptTeamApplication(teamId, applicationId, userId);

		return ResponseEntity.ok(ApiResponse.success(null));
	}

	@Operation(summary = "팀 가입 신청 거절 API", description = "팀 가입 신청을 거절합니다.")
	@RequireTeamRole(TeamRole.LEADER)
	@PatchMapping("/{teamId}/applications/{applicationId}/reject")
	public ResponseEntity<ApiResponse<Void>> rejectTeamApplication(
			@PathVariable Long teamId,
			@PathVariable Long applicationId,
			@AuthenticationPrincipal Long userId
	) {
		teamService.rejectTeamApplication(teamId, applicationId, userId);

		return ResponseEntity.ok(ApiResponse.success(null));
	}

	@Operation(summary = "팀 멤버 목록 조회 API", description = "팀 멤버 목록을 조회합니다.")
	@GetMapping("/{teamId}/members")
	public ResponseEntity<ApiResponse<List<TeamMemberResponse>>> getTeamMembers(
			@PathVariable Long teamId
	) {
		List<TeamMemberResponse> members = teamService.findTeamMembers(teamId);

		return ResponseEntity.ok(ApiResponse.success(members));
	}
}
