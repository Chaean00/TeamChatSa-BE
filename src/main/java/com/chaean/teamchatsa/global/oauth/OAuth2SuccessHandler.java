package com.chaean.teamchatsa.global.oauth;

import com.chaean.teamchatsa.domain.user.dto.response.TokenRes;
import com.chaean.teamchatsa.domain.user.service.OAuthService;
import com.chaean.teamchatsa.global.exception.BusinessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	private final OAuthService oAuthService;

	@Value("${app.auth.redirect-success}")
	private String redirectSuccess;

	@Value("${app.auth.redirect-failure}")
	private String redirectFailure;

	@Value("${spring.profiles.active:dev}")
	private String activeProfile;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
		log.info("[OAuth] Success Handler");
		OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
		OAuth2User principal = token.getPrincipal();
		Map<String, Object> attrs = principal.getAttributes();

		String providerUserId = String.valueOf(attrs.get("id"));
		Map<String, Object> kakaoAccount = attrs.containsKey("kakao_account") ? (Map<String, Object>) attrs.get("kakao_account") : null;

		String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
		Map<String, Object> properties = attrs.containsKey("properties") ? (Map<String, Object>) attrs.get("properties") : null;
		String nickname = properties != null ? (String) properties.get("nickname") : null;
		String profileImg = properties != null ? (String) properties.get("profile_image") : null;

		try {
			TokenRes tokens = oAuthService.loginByKakao(providerUserId, email, nickname, profileImg);
			// refresh Token 쿠카 설정
			ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokens.getRefreshToken())
					.httpOnly(true)
					.secure("prod".equals(activeProfile))       // 운영 HTTPS 필수
					.sameSite("None")   // 다른 도메인으로 리다이렉트한다면 None 필요
					.path("/")
					.maxAge(Duration.ofDays(14))
					.build();

			response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

			String url = redirectSuccess + "?token=" + URLEncoder.encode(tokens.getAccessToken(), StandardCharsets.UTF_8);
			getRedirectStrategy().sendRedirect(request, response, url);
		} catch (BusinessException e) {
			log.error("[OAuth] 카카오 로그인 중 에러가 발생했습니다.: {}", e.getMessage());
			String url = redirectFailure + "?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
			getRedirectStrategy().sendRedirect(request, response, url);
		}
	}
}
