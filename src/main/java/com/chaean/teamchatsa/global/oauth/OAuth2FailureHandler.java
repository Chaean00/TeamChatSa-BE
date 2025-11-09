package com.chaean.teamchatsa.global.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

	@Value("${app.auth.redirect-failure}")
	private String redirectFailure;

	@Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
	private String t;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
		log.info("[OAuth] Failure Handler");
		log.info("exception = {}", exception.getMessage());
		getRedirectStrategy().sendRedirect(request, response, redirectFailure);
	}
}
