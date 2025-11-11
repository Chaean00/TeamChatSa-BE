package com.chaean.teamchatsa.global.common.aop.aspect;

import com.chaean.teamchatsa.domain.team.model.TeamMember;
import com.chaean.teamchatsa.domain.team.model.TeamRole;
import com.chaean.teamchatsa.domain.team.repository.TeamMemberRepository;
import com.chaean.teamchatsa.global.common.aop.annotation.RequireTeamRole;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

/**
 * @RequireTeamRole 어노테이션이 붙은 메서드에 대한 팀 역할 기반 접근 제어를 처리하는 Aspect입니다.
 * 사용자의 팀 내 역할을 확인하여 지정된 역할을 가진 사용자만 메서드를 실행할 수 있도록 제한합니다.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class TeamRoleAspect {

    private final TeamMemberRepository teamMemberRepository;

    /**
     * @RequireTeamRole 어노테이션이 붙은 메서드 실행 전에 팀 역할을 검증
     * userId는 파라미터에서 찾지 못하면 SecurityContext에서 자동으로 추출
     */
    @Before("@annotation(requireTeamRole)")
    public void checkTeamRole(JoinPoint joinPoint, RequireTeamRole requireTeamRole) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        Parameter[] parameters = method.getParameters();

        // 파라미터에서 teamId 추출
        Long teamId = extractParameterValue(parameters, args, requireTeamRole.teamIdParam(), Long.class);

        if (teamId == null) {
            log.error("[팀 권한 검증 실패] teamId 파라미터를 찾을 수 없습니다. 파라미터명: {}", requireTeamRole.teamIdParam());
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 파라미터에서 userId 추출 시도, 없으면 SecurityContext에서 추출
        Long userId = extractParameterValue(parameters, args, requireTeamRole.userIdParam(), Long.class);
        if (userId == null) {
            userId = getUserIdFromSecurityContext();
        }

        // 람다에서 사용하기 위한 final 변수
        final Long finalUserId = userId;

        // 팀 멤버 조회
        TeamMember teamMember = teamMemberRepository
                .findByTeamIdAndUserIdAndIsDeletedFalse(teamId, finalUserId)
                .orElseThrow(() -> {
                    log.warn("[팀 권한 검증 실패] 사용자가 팀 멤버가 아닙니다. userId: {}, teamId: {}", finalUserId, teamId);
                    return new BusinessException(ErrorCode.NOT_TEAM_MEMBER);
                });

        // 요구되는 역할 확인
        TeamRole[] requiredRoles = requireTeamRole.value();
        TeamRole userRole = teamMember.getRole();

        boolean hasRequiredRole = Arrays.asList(requiredRoles).contains(userRole);

        if (!hasRequiredRole) {
            log.warn("[팀 권한 검증 실패] 사용자의 역할이 요구 조건을 충족하지 못합니다. userId: {}, 현재 역할: {}, 요구 역할: {}, teamId: {}",
                    finalUserId, userRole, Arrays.toString(requiredRoles), teamId);
            throw new BusinessException(ErrorCode.INSUFFICIENT_TEAM_ROLE);
        }

        log.info("[팀 권한 검증 성공] userId: {}, 역할: {}, teamId: {}", finalUserId, userRole, teamId);
    }

    /** SecurityContext에서 현재 인증된 사용자의 userId를 추출 */
    private Long getUserIdFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("[팀 권한 검증 실패] SecurityContext에 인증 정보가 없습니다.");
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Long) {
            return (Long) principal;
        }

        log.error("[팀 권한 검증 실패] principal 타입이 지원되지 않습니다. type: {}", principal.getClass().getName());
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }

    /** 메서드 파라미터에서 지정된 이름의 값을 추출 */
    private <T> T extractParameterValue(Parameter[] parameters, Object[] args, String paramName, Class<T> expectedType) {
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            // 파라미터 이름이 일치하는 경우
            if (parameter.getName().equals(paramName)) {
                Object value = args[i];
                if (value != null && expectedType.isInstance(value)) {
                    return expectedType.cast(value);
                }
            }

            // @PathVariable, @RequestParam 등의 어노테이션에서 value 확인
            for (Annotation annotation : parameter.getAnnotations()) {
                String annotationValue = getAnnotationValue(annotation);
                if (paramName.equals(annotationValue)) {
                    Object value = args[i];
                    if (value != null && expectedType.isInstance(value)) {
                        return expectedType.cast(value);
                    }
                }
            }
        }
        return null;
    }

    /** 어노테이션에서 value 속성 추출 */
    private String getAnnotationValue(Annotation annotation) {
        try {
            Method valueMethod = annotation.annotationType().getMethod("value");
            Object value = valueMethod.invoke(annotation);
            if (value instanceof String) {
                return (String) value;
            } else if (value instanceof String[]) {
                String[] values = (String[]) value;
                return values.length > 0 ? values[0] : null;
            }
        } catch (Exception e) {
            // value 메서드가 없거나 호출 실패 시 무시
        }
        return null;
    }
}
