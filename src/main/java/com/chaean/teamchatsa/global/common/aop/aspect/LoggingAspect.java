package com.chaean.teamchatsa.global.common.aop.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/**
 * @Loggable 어노테이션이 붙은 메서드에 대한 로깅을 처리하는 Aspect 입니다.
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("@annotation(com.chaean.teamchatsa.global.common.aop.annotation.Loggable)")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getMethod().getName();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        // 스레드 및 사용자 정보
        String threadName = Thread.currentThread().getName();
        String userInfo = getUserInfo();

        log.info("[START] {}.{}() | Thread: {} | User: {}", className, methodName, threadName, userInfo);

        if (parameterNames.length > 0) {
            for (int i = 0; i < parameterNames.length; i++) {
                log.info("  => {} ({}): {}", parameterNames[i], args[i] != null ? args[i].getClass().getSimpleName() : "null", args[i]);
            }
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object result;
        try {
            result = joinPoint.proceed();
            stopWatch.stop();
            log.info("[SUCCESS] {}.{}() | Thread: {} | User: {} | Execution Time: {} ms",
                    className, methodName, threadName, userInfo, stopWatch.getTotalTimeMillis());
            return result;
        } catch (Throwable throwable) {
            stopWatch.stop();
            log.error("[EXCEPTION] {}.{}() | Thread: {} | User: {} | Execution Time: {} ms",
                    className, methodName, threadName, userInfo, stopWatch.getTotalTimeMillis(), throwable);
            throw throwable;
        }
    }

    /** SecurityContext에서 인증된 사용자 정보를 가져옵니다. 인증되지 않은 경우 "익명"을 반환.*/
    private String getUserInfo() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof Long) {
                Long userId = (Long) authentication.getPrincipal();
                return "userId=" + userId;
            }
        } catch (Exception e) {
            // SecurityContext를 사용할 수 없는 경우 (예: 비동기 스레드)
            log.debug("SecurityContext에서 사용자 정보를 가져올 수 없습니다: {}", e.getMessage());
        }
        return "익명";
    }
}
