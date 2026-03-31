package com.chaean.teamchatsa.global.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

	@Value("${app.auth.redirect-failure}")
	private String redirectFailure;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
			throws IOException, ServletException {
		log.info("[OAuth] Failure Handler");
		log.info("[OAuth2FailureHandler] Exception = {}", exception.getMessage());
		String state = request.getParameter("state");
		String url = redirectFailure;
		if (state != null && !state.isBlank()) {
			url += "?state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);
		}
		getRedirectStrategy().sendRedirect(request, response, url);
	}
}
