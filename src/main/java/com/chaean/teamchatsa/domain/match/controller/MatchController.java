package com.chaean.teamchatsa.domain.match.controller;

import com.chaean.teamchatsa.domain.match.dto.request.MatchApplicationRequest;
import com.chaean.teamchatsa.domain.match.dto.request.MatchMapSearchRequest;
import com.chaean.teamchatsa.domain.match.dto.request.MatchPostCreateRequest;
import com.chaean.teamchatsa.domain.match.dto.request.MatchPostSearchRequest;
import com.chaean.teamchatsa.domain.match.dto.request.MatchRecommendationRequest;
import com.chaean.teamchatsa.domain.match.dto.request.MatchResultCreateRequest;
import com.chaean.teamchatsa.domain.match.dto.response.MatchApplicantResponse;
import com.chaean.teamchatsa.domain.match.dto.response.MatchMapResponse;
import com.chaean.teamchatsa.domain.match.dto.response.MatchPostDetailResponse;
import com.chaean.teamchatsa.domain.match.dto.response.MatchPostListResponse;
import com.chaean.teamchatsa.domain.match.dto.response.MatchRecommendationResponse;
import com.chaean.teamchatsa.domain.match.service.MatchRecommendationService;
import com.chaean.teamchatsa.domain.match.service.MatchResultService;
import com.chaean.teamchatsa.domain.match.service.MatchService;
import com.chaean.teamchatsa.domain.team.model.TeamRole;
import com.chaean.teamchatsa.global.common.aop.annotation.RequireTeamRole;
import com.chaean.teamchatsa.global.common.dto.ApiResponse;
import com.chaean.teamchatsa.global.common.dto.SliceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "매치 API", description = "매치 글 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/matches")
public class MatchController {

	private final MatchService matchService;
	private final MatchResultService matchResultService;
	private final MatchRecommendationService matchRecommendationService;

	@Operation(summary = "매치 포스트 생성 API", description = "새로운 매치 포스트를 생성합니다.")
	@PostMapping("")
	@RequireTeamRole({TeamRole.LEADER, TeamRole.CO_LEADER})
	public ResponseEntity<ApiResponse<Void>> createMatchPost(
			@AuthenticationPrincipal Long userId,
			@RequestBody @Valid MatchPostCreateRequest req
	) {
		matchService.registerMatchPost(userId, req);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("매치 게시물 등록 성공", null));
	}

	@Operation(summary = "경기 결과 등록 API", description = "경기가 종료된 후 결과를 등록합니다.")
	@PostMapping("/results")
	public ResponseEntity<ApiResponse<Void>> createMatchResult(
			@RequestBody @Validated MatchResultCreateRequest req
	) {
		matchResultService.registerMatchResult(req);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
	}

	@Operation(summary = "매치 게시물 삭제 API", description = "기존 매치 게시물을 삭제합니다.")
	@DeleteMapping("/{matchId}")
	@RequireTeamRole({TeamRole.LEADER, TeamRole.CO_LEADER})
	public ResponseEntity<ApiResponse<Void>> deleteMatchPost(
			@PathVariable Long matchId,
			@AuthenticationPrincipal Long userId
	) {
		matchService.deleteMatchPost(userId, matchId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success(null));
	}

	@Operation(summary = "매치 게시물 목록 조회 API", description = "매치 게시물 목록을 조회합니다.(무한스크롤)")
	@GetMapping("")
	public ResponseEntity<ApiResponse<SliceResponse<MatchPostListResponse>>> getMatches(
			@ModelAttribute MatchPostSearchRequest req
	) {
		SliceResponse<MatchPostListResponse> response = matchService.findMatchPosts(req);
		return ResponseEntity.ok(ApiResponse.success("매치 목록 조회 성공", response));
	}

	@Operation(summary = "AI 매치 추천 API", description = "사용자 검색어를 바탕으로 신청 가능한 매치 게시물을 추천합니다.")
	@PostMapping("/recommendations")
	public ResponseEntity<ApiResponse<List<MatchRecommendationResponse>>> recommendMatches(
			@AuthenticationPrincipal Long userId,
			@RequestBody @Validated MatchRecommendationRequest req
	) {
		List<MatchRecommendationResponse> response = matchRecommendationService.recommendMatches(userId, req);
		return ResponseEntity.ok(ApiResponse.success("매치 추천 조회 성공", response));
	}

	@Operation(summary = "위치 기반 매치 검색 API", description = "줌 레벨에 따라 최대 40개의 marker를 반환합니다.")
	@GetMapping("/map")
	public ResponseEntity<ApiResponse<List<MatchMapResponse>>> getMatchesByLocation(
			@ModelAttribute @Valid MatchMapSearchRequest req
	) {
		List<MatchMapResponse> matches = matchService.searchMatchesByLocation(req);
		return ResponseEntity.ok(ApiResponse.success("위치 기반 매치 검색 성공", matches));
	}

	@Operation(summary = "매치 게시물 상세 조회 API", description = "특정 매치 게시물의 상세 정보를 조회합니다.")
	@GetMapping("/{matchId}")
	public ResponseEntity<ApiResponse<MatchPostDetailResponse>> getMatchDetail(
			@PathVariable Long matchId
	) {
		return ResponseEntity.ok(
				ApiResponse.success("매치 상세 조회 성공", matchService.findMatchPostDetail(matchId)));
	}

	@Operation(summary = "매치 신청 API", description = "특정 매치 게시물에 팀이 신청합니다.")
	@PostMapping("/{matchId}/apply")
	@RequireTeamRole({TeamRole.LEADER, TeamRole.CO_LEADER})
	public ResponseEntity<ApiResponse<Void>> applyToMatch(
			@AuthenticationPrincipal Long userId,
			@PathVariable Long matchId,
			@RequestBody MatchApplicationRequest req
	) {
		matchService.registerMatchApplication(userId, matchId, req);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
	}

	@Operation(summary = "매치 신청 취소 API", description = "특정 매치 게시물에 대한 팀의 신청을 취소합니다.")
	@PostMapping("/{matchId}/cancel")
	@RequireTeamRole({TeamRole.LEADER, TeamRole.CO_LEADER})
	public ResponseEntity<ApiResponse<Void>> cancelMatchApplication(
			@PathVariable Long matchId,
			@AuthenticationPrincipal Long userId
	) {
		matchService.deleteMatchApplication(userId, matchId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT)
				.body(ApiResponse.success("매치 신청이 취소되었습니다.", null));
	}

	@Operation(summary = "매치 신청 수락 API", description = "특정 매치 게시물에 대한 팀의 신청을 수락합니다.")
	@PostMapping("/{matchId}/accept/{applicantId}")
	@RequireTeamRole({TeamRole.LEADER, TeamRole.CO_LEADER})
	public ResponseEntity<ApiResponse<Void>> acceptMatchApplication(
			@PathVariable Long matchId,
			@PathVariable Long applicantId,
			@AuthenticationPrincipal Long userId
	) {
		String teamName = matchService.acceptMatchApplication(matchId, applicantId, userId);
		String message = "매치 신청이 수락되었습니다. 팀명: " + teamName;
		return ResponseEntity.ok(ApiResponse.success(message, null));
	}

	@Operation(summary = "매치 신청 거절 API", description = "특정 매치 게시물에 대한 팀의 신청을 거절합니다.")
	@PostMapping("/{matchId}/reject/{applicantId}")
	@RequireTeamRole({TeamRole.LEADER, TeamRole.CO_LEADER})
	public ResponseEntity<ApiResponse<Void>> rejectMatchApplication(
			@PathVariable Long matchId,
			@PathVariable Long applicantId,
			@AuthenticationPrincipal Long userId
	) {
		String teamName = matchService.rejectMatchApplication(matchId, applicantId, userId);
		String message = "매치 신청이 거절되었습니다. 팀명: " + teamName;
		return ResponseEntity.ok(ApiResponse.success(message, null));
	}

	@Operation(summary = "매치 신청 팀 목록 조회 API", description = "특정 매치 게시물에 신청한 팀들의 목록을 조회합니다.")
	@GetMapping("/{matchId}/applicants")
	@RequireTeamRole({TeamRole.LEADER, TeamRole.CO_LEADER})
	public ResponseEntity<ApiResponse<List<MatchApplicantResponse>>> getMatchApplicants(
			@PathVariable Long matchId,
			@AuthenticationPrincipal Long userId
	) {
		List<MatchApplicantResponse> applicants = matchService.getMatchApplicants(userId, matchId);
		return ResponseEntity.ok(ApiResponse.success("매치 신청 팀 목록 조회 성공", applicants));
	}

	@Operation(summary = "특정 팀의 매치 게시물 목록 조회 API", description = "특정 팀이 작성한 매치 게시물 목록을 조회합니다.")
	@GetMapping("/{teamId}/team-posts")
	public ResponseEntity<ApiResponse<SliceResponse<MatchPostListResponse>>> getMatchByTeamId(
			@PathVariable Long teamId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size
	) {
		SliceResponse<MatchPostListResponse> response = matchService.findMatchPostListByTeamId(teamId, page,
				size);
		return ResponseEntity.ok(ApiResponse.success("특정 팀의 매치 게시물 목록 조회 성공", response));
	}
}
