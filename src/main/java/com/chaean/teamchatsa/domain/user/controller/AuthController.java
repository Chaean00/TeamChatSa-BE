package com.chaean.teamchatsa.domain.user.controller;

import com.chaean.teamchatsa.domain.user.dto.requset.LoginReq;
import com.chaean.teamchatsa.domain.user.dto.response.LoginRes;
import com.chaean.teamchatsa.domain.user.dto.requset.SignupReq;
import com.chaean.teamchatsa.domain.user.dto.response.TokenRes;
import com.chaean.teamchatsa.domain.user.service.AuthService;
import com.chaean.teamchatsa.global.common.dto.ApiResponse;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {

	@Value("${spring.profiles.active:dev}")
	private String activeProfile;
	private final AuthService authService;

	@PostMapping("/signup")
	public ResponseEntity<ApiResponse<Void>> signup(@RequestBody @Validated SignupReq req) {
		authService.signup(req);

		return ResponseEntity.status(201).body(ApiResponse.success("회원가입에 성공했습니다.", null));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginRes>> login(@RequestBody @Validated LoginReq req) {
		LoginRes loginRes = authService.login(req);

		// RefreshToken을 HttpOnly 쿠키로 설정
		ResponseCookie refreshCookie = createRefreshTokenCookie(loginRes.refreshToken());

		return ResponseEntity.status(200)
				.header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
				.body(ApiResponse.success("로그인에 성공했습니다.", new LoginRes(loginRes.accessToken(), null)));
	}

	@PostMapping("/reissue")
	public ResponseEntity<ApiResponse<TokenRes>> refreshToken(
			@CookieValue(name = "refreshToken", required = false) String refreshToken
	) {
		if (refreshToken == null) {
			throw new BusinessException(ErrorCode.TOKEN_EXPIRED, "리프레시 토큰이 존재하지 않습니다.");
		}

		TokenRes res = authService.reissueToken(refreshToken);

		ResponseCookie refreshCookie = createRefreshTokenCookie(res.refreshToken());

		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
				.body(ApiResponse.success("토큰 갱신에 성공했습니다.",
					new TokenRes(res.accessToken(), null)));
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal Long userId) {
		authService.logout(userId);

		// RefreshToken 쿠키 삭제
		ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
				.maxAge(0)
				.httpOnly(true) // XSS 방지
				.secure("prod".equals(activeProfile))
				.sameSite("None") // CSRF 방지
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
				.secure(true) // TODO : 운영환경에서는 true로 변경 필요 -> HTTPS 에서만 전송
				.sameSite("None") // CSRF 방지
				.path("/")
				.build();
	}

	/** 쿠키에서 RefreshToken 추출*/
	private String extractRefreshTokenFromCookie(HttpServletRequest request) {
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if ("refreshToken".equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}
}
