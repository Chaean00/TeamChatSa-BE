package com.chaean.teamchatsa.global.common.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 분산 락 어노테이션
 * - Redis 기반 분산 락을 적용하여 동시성 제어
 * - SpEL 표현식을 통한 동적 락 키 생성 지원
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

	/**
	 * 락의 키 값 (SpEL 표현식 지원)
	 * 예: "match:#{#matchId}", "match:#{#matchId}:application:#{#applicantId}"
	 */
	String key();

	/**
	 * 락 획득을 시도하는 최대 대기 시간 (ms)
	 * 이 시간 동안 락 획득에 실패하면 예외 발생
	 */
	long waitTime() default 3000L;

	/**
	 * 락을 획득한 후 자동으로 해제되는 시간 (ms)
	 * 메서드 실행이 이 시간을 초과하면 자동으로 락이 해제됨
	 */
	long leaseTime() default 5000L;

	/**
	 * 시간 단위
	 */
	TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}
