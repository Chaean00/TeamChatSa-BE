package com.chaean.teamchatsa.global.common.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메서드 실행 시간 및 로그를 기록하기 위한 어노테이션입니다.
 * 이 어노테이션이 붙은 메서드는 LoggingAspect에 의해 실행 정보가 로깅됩니다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Loggable {
}
