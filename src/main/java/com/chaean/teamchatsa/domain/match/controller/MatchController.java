package com.chaean.teamchatsa.domain.match.controller;

import com.chaean.teamchatsa.domain.match.dto.request.MatchApplicationReq;
import com.chaean.teamchatsa.domain.match.dto.request.MatchPostCreateReq;
import com.chaean.teamchatsa.domain.match.dto.response.MatchApplicantRes;
import com.chaean.teamchatsa.domain.match.dto.response.MatchLocationRes;
import com.chaean.teamchatsa.domain.match.dto.response.MatchPostDetailRes;
import com.chaean.teamchatsa.domain.match.dto.response.MatchPostListRes;
import com.chaean.teamchatsa.domain.match.service.MatchService;
import com.chaean.teamchatsa.domain.team.model.TeamRole;
import com.chaean.teamchatsa.global.common.aop.annotation.RequireTeamRole;
import com.chaean.teamchatsa.global.common.dto.ApiResponse;
import com.chaean.teamchatsa.global.common.dto.SliceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "매치 API", description = "매치 글 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/matches")
public class MatchController {

	private final MatchService matchService;

	@Operation(summary = "매치 게시물 등록 API", description = "새로운 매치 게시물을 등록합니다.")
	@PostMapping("")
	@RequireTeamRole({TeamRole.LEADER, TeamRole.CO_LEADER})
	public ResponseEntity<ApiResponse<Void>> createMatchPost(
			@AuthenticationPrincipal Long userId,
			@RequestBody @Valid MatchPostCreateReq req
	) {
		matchService.registerMatchPost(userId, req);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("매치 게시물 등록 성공", null));
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
	public ResponseEntity<ApiResponse<Slice<MatchPostListRes>>> getMatchList(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size
	) {
		return ResponseEntity.ok(ApiResponse.success("매치 목록 조회 성공", matchService.findMatchPostList(page, size)));
	}

	@Operation(summary = "위치 기반 매치 검색 API", description = "사용자의 위치를 기준으로 주변의 매치 게시물을 검색합니다. (무한 스크롤)")
	@GetMapping("/map")
	public ResponseEntity<ApiResponse<SliceResponse<MatchLocationRes>>> searchNearbyMatches(
			@RequestParam Double lat,
			@RequestParam Double lng,
			@RequestParam(defaultValue = "5000") Double radius,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size
	) {
		SliceResponse<MatchLocationRes> matches = matchService.searchMatchesByLocation(lat, lng, radius, page, size);
		return ResponseEntity.ok(ApiResponse.success("위치 기반 매치 검색 성공", matches));
	}

	@Operation(summary = "매치 게시물 상세 조회 API", description = "특정 매치 게시물의 상세 정보를 조회합니다.")
	@GetMapping("/{matchId}")
	public ResponseEntity<ApiResponse<MatchPostDetailRes>> getMatchDetail(
			@PathVariable Long matchId
	) {
		return ResponseEntity.ok(ApiResponse.success("매치 상세 조회 성공", matchService.findMatchPostDetail(matchId)));
	}

	@Operation(summary = "매치 신청 API", description = "특정 매치 게시물에 팀이 신청합니다.")
	@PostMapping("/{matchId}/apply")
	@RequireTeamRole({TeamRole.LEADER, TeamRole.CO_LEADER})
	public ResponseEntity<ApiResponse<Void>> applyToMatch(
			@AuthenticationPrincipal Long userId,
			@PathVariable Long matchId,
			@RequestBody MatchApplicationReq req
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
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success("매치 신청이 취소되었습니다.",null));
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
		return ResponseEntity.ok(ApiResponse.success(message,null));
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
		return ResponseEntity.ok(ApiResponse.success(message,null));
	}

	@Operation(summary = "매치 신청 팀 목록 조회 API", description = "특정 매치 게시물에 신청한 팀들의 목록을 조회합니다.")
	@GetMapping("/{matchId}/applicants")
	@RequireTeamRole({TeamRole.LEADER, TeamRole.CO_LEADER})
	public ResponseEntity<ApiResponse<List<MatchApplicantRes>>> getMatchApplicants(
			@PathVariable Long matchId,
			@AuthenticationPrincipal Long userId
	) {
		List<MatchApplicantRes> applicants = matchService.getMatchApplicants(userId, matchId);
		return ResponseEntity.ok(ApiResponse.success("매치 신청 팀 목록 조회 성공", applicants));
	}

	@Operation(summary = "특정 팀의 매치 게시물 목록 조회 API", description = "특정 팀이 작성한 매치 게시물 목록을 조회합니다.")
	@GetMapping("/{teamId}/team-posts")
	public ResponseEntity<ApiResponse<Slice<MatchPostListRes>>> getMatchByTeamId(
			@PathVariable Long teamId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size
	) {
		Slice<MatchPostListRes> matchPosts = matchService.findMatchPostListByTeamId(teamId, page, size);
		return ResponseEntity.ok(ApiResponse.success("특정 팀의 매치 게시물 목록 조회 성공", matchPosts));
	}
}
