package com.chaean.teamchatsa.domain.match.controller;

import com.chaean.teamchatsa.domain.match.dto.request.MatchApplicationReq;
import com.chaean.teamchatsa.domain.match.dto.request.MatchPostCreateReq;
import com.chaean.teamchatsa.domain.match.dto.response.MatchPostDetailRes;
import com.chaean.teamchatsa.domain.match.dto.response.MatchPostListRes;
import com.chaean.teamchatsa.domain.match.service.MatchService;
import com.chaean.teamchatsa.domain.team.model.TeamRole;
import com.chaean.teamchatsa.global.common.aop.annotation.RequireTeamRole;
import com.chaean.teamchatsa.global.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/matches")
public class MatchController {

	private final MatchService matchService;

	@PostMapping("")
	@RequireTeamRole({TeamRole.LEADER, TeamRole.CO_LEADER})
	public ResponseEntity<ApiResponse<Void>> createMatchPost(
			@AuthenticationPrincipal Long userId,
			@RequestBody @Valid MatchPostCreateReq req
	) {
		matchService.registerMatchPost(userId, req);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("매치 게시물 등록 성공", null));
	}

	@DeleteMapping("/{matchId}")
	@RequireTeamRole({TeamRole.LEADER, TeamRole.CO_LEADER})
	public ResponseEntity<ApiResponse<Void>> deleteMatchPost(
			@PathVariable Long matchId,
			@AuthenticationPrincipal Long userId
	) {
		matchService.deleteMatchPost(userId, matchId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success(null));
	}

	@GetMapping("")
	public ResponseEntity<ApiResponse<Slice<MatchPostListRes>>> getMatchList(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size
	) {
		return ResponseEntity.ok(ApiResponse.success("매치 목록 조회 성공", matchService.findMatchPostList(page, size)));
	}

	@GetMapping("/{matchId}")
	public ResponseEntity<ApiResponse<MatchPostDetailRes>> getMatchDetail(
			@PathVariable Long matchId
	) {
		return ResponseEntity.ok(ApiResponse.success("매치 상세 조회 성공", matchService.findMatchPostDetail(matchId)));
	}

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

	@PostMapping("/{matchId}/cancel")
	@RequireTeamRole({TeamRole.LEADER, TeamRole.CO_LEADER})
	public ResponseEntity<ApiResponse<Void>> cancelMatchApplication(
			@PathVariable Long matchId,
			@AuthenticationPrincipal Long userId
	) {
		matchService.deleteMatchApplication(userId, matchId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success("매치 신청이 취소되었습니다.",null));
	}

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
}
