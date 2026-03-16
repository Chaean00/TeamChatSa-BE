package com.chaean.teamchatsa.domain.user.controller;

import com.chaean.teamchatsa.domain.user.dto.requset.PasswordUpdateRequest;
import com.chaean.teamchatsa.domain.user.dto.requset.UserUpdateRequest;
import com.chaean.teamchatsa.domain.user.dto.response.UserResponse;
import com.chaean.teamchatsa.domain.user.service.UserService;
import com.chaean.teamchatsa.global.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "유저 API", description = "유저 관련 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@Operation(summary = "유저 프로필 조회 API", description = "로그인한 유저의 프로필을 조회합니다.")
	@GetMapping("")
	public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(@AuthenticationPrincipal Long userId) {
		UserResponse res = userService.findUser(userId);

		return ResponseEntity.ok(ApiResponse.success("유저 조회에 성공했습니다", res));
	}

	@Operation(summary = "유저 프로필 수정 API", description = "로그인한 유저의 프로필을 수정합니다.")
	@PatchMapping("")
	public ResponseEntity<ApiResponse<Void>> updateUserProfile(@AuthenticationPrincipal Long userId,
			@RequestBody @Valid UserUpdateRequest req) {
		userService.updateUser(userId, req);

		return ResponseEntity.ok(ApiResponse.success(null));
	}

	@Operation(summary = "유저 비밀번호 수정 API", description = "로그인한 유저의 비밀번호를 수정합니다.")
	@PutMapping("/password")
	public ResponseEntity<ApiResponse<Void>> updateUserPassword(@AuthenticationPrincipal Long userId,
			@RequestBody @Valid PasswordUpdateRequest req) {
		userService.updatePassword(userId, req);

		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success(null));
	}

	@Operation(summary = "닉네임 중복 체크 API", description = "닉네임의 중복 여부를 확인합니다.")
	@GetMapping("/check")
	public ResponseEntity<ApiResponse<Boolean>> checkNickname(@RequestParam String nickname) {
		Boolean exists = userService.existsByNickname(nickname);

		return ResponseEntity.ok(ApiResponse.success("사용가능한 닉네임입니다.", exists));
	}

	@Operation(summary = "유저 탈퇴 API", description = "로그인한 유저의 계정을 탈퇴합니다.")
	@DeleteMapping("")
	public ResponseEntity<ApiResponse<Void>> deleteUserProfile(@AuthenticationPrincipal Long userId) {
		userService.deleteUser(userId);

		return ResponseEntity.ok(ApiResponse.success("이용해주셔서 감사합니다.", null));
	}
}
