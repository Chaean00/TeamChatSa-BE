package com.chaean.teamchatsa.global.config;

import com.chaean.teamchatsa.infra.slack.SlackAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@RequiredArgsConstructor
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

	private final SlackAlertService slackAlertService;

	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("async-notification-");
		executor.initialize();
		return executor;
	}

	/** 비동기 메서드 예외 처리 */
	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return (ex, method, params) -> {
			log.error("비동기 메서드 실행 중 예외 발생: method={}, params={}, error={}",
					method.getName(), params, ex.getMessage(), ex);

			// 🚨 Slack 알림 전송
			slackAlertService.sendAsyncFailureAlert(
					method.getName(),
					ex.getMessage(),
					params
			);
		};
	}
}
