package com.chaean.teamchatsa.domain.user.controller;

import com.chaean.teamchatsa.domain.user.dto.requset.PasswordUpdateReq;
import com.chaean.teamchatsa.domain.user.dto.requset.UserUpdateReq;
import com.chaean.teamchatsa.domain.user.dto.response.UserRes;
import com.chaean.teamchatsa.domain.user.service.UserService;
import com.chaean.teamchatsa.global.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping("")
	public ResponseEntity<ApiResponse<UserRes>> getUserProfile(@AuthenticationPrincipal Long userId) {
		UserRes res = userService.findUser(userId);

		return ResponseEntity.ok(ApiResponse.success("유저 조회에 성공했습니다", res));
	}

	@PatchMapping("")
	public ResponseEntity<ApiResponse<Void>> updateUserProfile(@AuthenticationPrincipal Long userId, @RequestBody UserUpdateReq req) {
		userService.updateUser(userId, req);

		return ResponseEntity.ok(ApiResponse.success(null));
	}

	@PutMapping("/password")
	public ResponseEntity<ApiResponse<Void>> updateUserPassword(@AuthenticationPrincipal Long userId, @RequestBody PasswordUpdateReq req) {
		userService.updatePassword(userId, req);

		return ResponseEntity.status(HttpStatus.NO_CONTENT).body( ApiResponse.success(null));
	}

	@GetMapping("/check")
	public ResponseEntity<ApiResponse<Boolean>> checkNickname(@RequestParam String nickname) {
		Boolean exists = userService.existsByNickname(nickname);

		return ResponseEntity.ok(ApiResponse.success("사용가능한 닉네임입니다.", exists));
	}

	@DeleteMapping("")
	public ResponseEntity<ApiResponse<Void>> deleteUserProfile(@AuthenticationPrincipal Long userId) {
		userService.deleteUser(userId);

		return ResponseEntity.ok(ApiResponse.success("이용해주셔서 감사합니다.", null));
	}
}
