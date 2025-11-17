package com.chaean.teamchatsa.global.config;

import com.chaean.teamchatsa.global.common.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * API 요청에 대해 OAuth2 리다이렉트 대신 JSON 응답을 반환하는 EntryPoint
 */
@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {

        String requestURI = request.getRequestURI();
        log.error("인증 실패 요청: {} - {}", requestURI, authException.getMessage());

        // API 요청인 경우 JSON 응답 반환
        if (requestURI.startsWith("/api/")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            ObjectMapper mapper = new ObjectMapper();
            ApiResponse<Void> apiResponse = ApiResponse.fail("인증이 필요합니다.");

            response.getWriter().write(mapper.writeValueAsString(apiResponse));
            return;
        }

        // OAuth2 로그인 페이지 요청이 아닌 다른 경우 기본 처리
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}