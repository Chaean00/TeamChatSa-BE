package com.chaean.teamchatsa.domain.user.controller;

import com.chaean.teamchatsa.domain.user.dto.requset.LoginReq;
import com.chaean.teamchatsa.domain.user.dto.response.LoginRes;
import com.chaean.teamchatsa.domain.user.dto.requset.SignupReq;
import com.chaean.teamchatsa.domain.user.service.AuthService;
import com.chaean.teamchatsa.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {
	private final AuthService authService;

	@PostMapping("/signup")
	public ResponseEntity<ApiResponse<Void>> signup(@RequestBody SignupReq req) {
		authService.signup(req);

		return ResponseEntity.status(201).body(ApiResponse.success("회원가입에 성공했습니다.", null));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginRes>> login(@RequestBody LoginReq req) {
		LoginRes res = authService.login(req);

		return ResponseEntity.status(200).body(ApiResponse.success("로그인에 성공했습니다.", res));
	}
}
