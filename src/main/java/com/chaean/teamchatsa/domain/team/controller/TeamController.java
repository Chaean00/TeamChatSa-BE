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

	/**
	 * Create a new team.
	 *
	 * @param userId the ID of the authenticated user creating the team
	 * @param req the validated request payload containing the team's creation details
	 * @return an ApiResponse with no data and an HTTP 201 Created status
	 */
	@Operation(summary = "팀 생성 API", description = "새로운 팀을 생성합니다.")
	@PostMapping("")
	public ResponseEntity<ApiResponse<Void>> createTeam(
			@AuthenticationPrincipal Long userId,
			@RequestBody @Validated TeamCreateRequest req
	) {
		teamService.registerTeam(userId, req);

		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
	}

	/**
	 * Register a review for an opponent team after a completed match.
	 *
	 * @param userId the ID of the authenticated user submitting the review
	 * @param req the review creation request containing review details
	 * @return an ApiResponse with null data indicating the review was created
	 */
	@Operation(summary = "팀 리뷰 등록 API", description = "경기가 끝난 상대 팀에 대한 리뷰를 등록합니다.")
	@PostMapping("/reviews")
	public ResponseEntity<ApiResponse<Void>> createReview(
			@AuthenticationPrincipal Long userId,
			@RequestBody @Validated TeamReviewCreateRequest req
	) {
		teamReviewService.registerReview(userId, req);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
	}

	/**
	 * Retrieve a slice of teams for infinite-scroll pagination.
	 *
	 * @param area     optional area filter to restrict teams by geographic region
	 * @param level    optional level filter to restrict teams by skill or rank
	 * @param page     zero-based page index
	 * @param size     number of items per page
	 * @param teamName optional team name filter for partial matching
	 * @return         an ApiResponse containing a SliceResponse of TeamListResponse with the requested slice of teams
	 */
	@Operation(summary = "팀 목록 조회 API", description = "팀 목록을 페이징 처리하여 조회합니다. (무한스크롤)")
	@GetMapping("")
	public ResponseEntity<ApiResponse<SliceResponse<TeamListResponse>>> getTeams(
			@RequestParam(required = false) String area,
			@RequestParam(required = false) String level,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String teamName
	) {
		SliceResponse<TeamListResponse> response = teamService.findTeamList(page, size, teamName);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * Retrieve detailed information for a specific team.
	 *
	 * @param teamId the ID of the team to retrieve
	 * @param userId the authenticated user's ID used to tailor the response (may be null)
	 * @return ApiResponse wrapping the team's detailed information
	 */
	@Operation(summary = "팀 상세 조회 API", description = "팀 상세 정보를 조회합니다.")
	@GetMapping("/{teamId}")
	public ResponseEntity<ApiResponse<TeamDetailResponse>> getTeamDetail(
			@PathVariable Long teamId,
			@AuthenticationPrincipal Long userId
	) {
		TeamDetailResponse teamDetail = teamService.findTeamDetail(teamId, userId);

		return ResponseEntity.ok(ApiResponse.success(teamDetail));
	}

	/**
	 * Submit a membership application to the specified team.
	 *
	 * @param userId the authenticated user's ID submitting the application
	 * @param teamId the ID of the team to apply to
	 * @param req the application payload containing join details
	 * @return an ApiResponse with no data indicating the application was created
	 */
	@Operation(summary = "팀 가입 신청 API", description = "팀에 가입 신청을 합니다.")
	@PostMapping("/{teamId}/applications")
	public ResponseEntity<ApiResponse<Void>> applyToTeam(
			@AuthenticationPrincipal Long userId,
			@PathVariable Long teamId,
			@RequestBody @Validated TeamJoinRequest req) {
		teamService.applyToTeam(teamId, userId, req);

		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
	}

	/**
	 * Retrieve membership applications for a team.
	 *
	 * @param teamId the ID of the team whose applications are being retrieved
	 * @param userId the ID of the authenticated requester (used for authorization/context)
	 * @return a list of TeamApplicationResponse objects representing the team's membership applications
	 */
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

	/**
	 * Accepts a pending membership application for the specified team.
	 *
	 * @param teamId        the ID of the team
	 * @param applicationId the ID of the membership application to accept
	 * @param userId        the ID of the authenticated user performing the acceptance
	 * @return              an ApiResponse with null data indicating successful acceptance
	 */
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

	/**
	 * Rejects a pending membership application to a team.
	 *
	 * @param teamId        the identifier of the team whose application is being rejected
	 * @param applicationId the identifier of the membership application to reject
	 * @param userId        the authenticated user's id performing the rejection (must have leader role)
	 * @return              an ApiResponse with no data that indicates the operation succeeded
	 */
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

	/**
	 * Retrieve the member list for a team.
	 *
	 * @param teamId the ID of the team whose members are being retrieved
	 * @return a list of team members wrapped in an ApiResponse
	 */
	@Operation(summary = "팀 멤버 목록 조회 API", description = "팀 멤버 목록을 조회합니다.")
	@GetMapping("/{teamId}/members")
	public ResponseEntity<ApiResponse<List<TeamMemberResponse>>> getTeamMembers(
			@PathVariable Long teamId
	) {
		List<TeamMemberResponse> members = teamService.findTeamMembers(teamId);

		return ResponseEntity.ok(ApiResponse.success(members));
	}
}
