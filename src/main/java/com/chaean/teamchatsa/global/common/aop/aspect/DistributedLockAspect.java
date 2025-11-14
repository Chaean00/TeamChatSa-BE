package com.chaean.teamchatsa.global.common.aop.aspect;

import com.chaean.teamchatsa.global.common.aop.annotation.DistributedLock;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

/**
 * @DistributedLock 분산 락 구현
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAspect {

	private final RedissonClient redissonClient;
	private final ExpressionParser parser = new SpelExpressionParser();

	@Around("@annotation(distributedLock)")
	public Object lock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		String[] parameterNames = signature.getParameterNames();
		Object[] args = joinPoint.getArgs();

		StandardEvaluationContext context = new StandardEvaluationContext();
		for (int i = 0; i < parameterNames.length; i++) {
			context.setVariable(parameterNames[i], args[i]);
		}

		String lockKey = parser.parseExpression(distributedLock.key()).getValue(context, String.class);
		RLock lock = redissonClient.getLock(lockKey);

		log.info("[분산 락] 락 획득 시도: {}", lockKey);

		try {
			// 락 획득 시도
			boolean acquired = lock.tryLock(
					distributedLock.waitTime(),
					distributedLock.leaseTime(),
					distributedLock.timeUnit()
			);

			if (!acquired) {
				log.error("[분산 락] 락 획득 실패: {}", lockKey);
				throw new BusinessException(ErrorCode.LOCK_ACQUISITION_FAILED, "다른 요청이 처리 중입니다. 잠시 후 다시 시도해주세요.");
			}

			log.info("[분산 락] 락 획득 성공: {}", lockKey);

			// 비즈니스 로직 실행
			return joinPoint.proceed();

		} finally {
			// 락 해제
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
				log.info("[분산 락] 락 해제 완료: {}", lockKey);
			}
		}
	}
}
