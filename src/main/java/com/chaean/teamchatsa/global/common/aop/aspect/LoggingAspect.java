package com.chaean.teamchatsa.global.common.aop.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
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

        log.info("[START] {}.{}()", className, methodName);

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
            log.info("[SUCCESS] {}.{}(). Execution Time: {} ms", className, methodName, stopWatch.getTotalTimeMillis());
            return result;
        } catch (Throwable throwable) {
            stopWatch.stop();
            log.error("[EXCEPTION] {}.{}(). Execution Time: {} ms", className, methodName, stopWatch.getTotalTimeMillis(), throwable);
            throw throwable;
        }
    }
}
