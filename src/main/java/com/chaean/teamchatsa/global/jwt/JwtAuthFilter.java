package com.chaean.teamchatsa.global.jwt;

import com.chaean.teamchatsa.domain.user.model.User;
import com.chaean.teamchatsa.domain.user.repository.UserRepository;
import com.chaean.teamchatsa.global.common.dto.ApiResponse;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final UserRepository userRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain) throws ServletException, IOException {
        String header = req.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                Long userId = jwtProvider.parseUserId(token);
                Optional<User> user = userRepo.findByIdAndIsDeletedFalse(userId);
                if (user.isEmpty()) {
                    throw new BusinessException(ErrorCode.USER_NOT_FOUND);
                }
                UsernamePasswordAuthenticationToken auth
                        = new UsernamePasswordAuthenticationToken(user.get().getId(), null, List.of(new SimpleGrantedAuthority(user.get().getRole().name())));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException e) {
                log.debug("유효하지 않은 토큰입니다. {}", e.getMessage(), e);
                res.setStatus(HttpStatus.UNAUTHORIZED.value());
                res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                res.setCharacterEncoding("UTF-8");

                ObjectMapper mapper = new ObjectMapper();
                ApiResponse<Void> apiResponse = ApiResponse.fail("JWT 오류가 발생했습니다.");
                res.getWriter().write(mapper.writeValueAsString(apiResponse));
                return; // 필터 체인 중단
            } catch (Exception e) {
                log.error("인증 처리 중 오류: {}", e.getMessage(), e);

                res.setStatus(HttpStatus.UNAUTHORIZED.value());
                res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                res.setCharacterEncoding("UTF-8");

                ObjectMapper mapper = new ObjectMapper();
                ApiResponse<Void> apiResponse = ApiResponse.fail("인증 오류가 발생했습니다.");
                res.getWriter().write(mapper.writeValueAsString(apiResponse));
                return; // 필터 체인 중단
            }
        }

        filterChain.doFilter(req, res);
    }
}
