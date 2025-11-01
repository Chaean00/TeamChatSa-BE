package com.chaean.teamchatsa.global.oauth;

import com.chaean.teamchatsa.domain.user.service.OAuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	private final OAuthService oAuthService;

	@Value("${app.auth.redirect-success}")
	private String redirectSuccess;

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

		String jwt = oAuthService.loginByKakao(providerUserId, email, nickname, profileImg);

		String url = redirectSuccess + "?token=" + URLEncoder.encode(jwt, StandardCharsets.UTF_8);
		getRedirectStrategy().sendRedirect(request, response, url);
	}
}
