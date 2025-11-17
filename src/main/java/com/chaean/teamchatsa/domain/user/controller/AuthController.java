package com.chaean.teamchatsa.domain.user.controller;

import com.chaean.teamchatsa.domain.user.dto.requset.LoginReq;
import com.chaean.teamchatsa.domain.user.dto.response.LoginRes;
import com.chaean.teamchatsa.domain.user.dto.requset.SignupReq;
import com.chaean.teamchatsa.domain.user.dto.response.TokenRes;
import com.chaean.teamchatsa.domain.user.service.AuthService;
import com.chaean.teamchatsa.global.common.dto.ApiResponse;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.time.Duration;


@Tag(name = "인증 API", description = "인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

	@Value("${spring.profiles.active:dev}")
	private String activeProfile;
	private final AuthService authService;

	@Operation(summary = "회원가입 API", description = "새로운 사용자를 회원가입 시킵니다.")
	@PostMapping("/signup")
	public ResponseEntity<ApiResponse<Void>> signup(@RequestBody @Valid SignupReq req) {
		authService.signup(req);

		return ResponseEntity.status(201).body(ApiResponse.success("회원가입에 성공했습니다.", null));
	}

	@Operation(summary = "로그인 API", description = "사용자 로그인 및 토큰 발급을 수행합니다.")
	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginRes>> login(@RequestBody @Valid LoginReq req) {
		LoginRes loginRes = authService.login(req);

		// RefreshToken을 HttpOnly 쿠키로 설정
		ResponseCookie refreshCookie = createRefreshTokenCookie(loginRes.getRefreshToken());

		return ResponseEntity.status(200)
				.header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
				.body(ApiResponse.success("로그인에 성공했습니다.", new LoginRes(loginRes.getAccessToken(), null)));
	}

	@Operation(summary = "토큰 재발급 API", description = "만료된 액세스 토큰을 리프레시 토큰으로 재발급합니다.")
	@PostMapping("/reissue")
	public ResponseEntity<ApiResponse<TokenRes>> refreshToken(
			@CookieValue(name = "refreshToken", required = false) String refreshToken
	) {
		if (refreshToken == null) {
			throw new BusinessException(ErrorCode.TOKEN_EXPIRED, "리프레시 토큰이 존재하지 않습니다.");
		}

		TokenRes res = authService.reissueToken(refreshToken);

		ResponseCookie refreshCookie = createRefreshTokenCookie(res.getRefreshToken());

		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
				.body(ApiResponse.success("토큰 갱신에 성공했습니다.",
					new TokenRes(res.getAccessToken(), null)));
	}

	@Operation(summary = "로그아웃 API", description = "사용자 로그아웃을 수행합니다.")
	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal Long userId) {
		authService.logout(userId);

		// RefreshToken 쿠키 삭제
		ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
				.maxAge(0)
				.httpOnly(true) // XSS 방지
				.secure("prod".equals(activeProfile))
				.sameSite("prod".equals(activeProfile) ? "None" : "Lax") // 로컬: Lax, 운영: None
				.path("/")
				.build();

		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
				.body(ApiResponse.success("로그아웃에 성공했습니다.", null));
	}

	/** RefreshToken 쿠키 생성 */
	private ResponseCookie createRefreshTokenCookie(String refreshToken) {
		return ResponseCookie.from("refreshToken", refreshToken)
				.maxAge(Duration.ofDays(14))
				.httpOnly(true) // XSS 방지
				.secure("prod".equals(activeProfile))
				.sameSite("prod".equals(activeProfile) ? "None" : "Lax") // 로컬: Lax, 운영: None
				.path("/")
				.build();
	}
}
