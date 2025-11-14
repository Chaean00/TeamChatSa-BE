package com.chaean.teamchatsa.global.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

	@Value("${spring.data.redis.host}")
	private String redisHost;

	@Value("${spring.data.redis.port}")
	private int redisPort;

	@Bean
	public RedissonClient redissonClient() {
		Config config = new Config();

		// Single Server 모드
		config.useSingleServer()
				.setAddress("redis://" + redisHost + ":" + redisPort)
				.setConnectionPoolSize(50)          // 커넥션 풀 최대 크기
				.setConnectionMinimumIdleSize(10)   // 최소 유휴 커넥션 수
				.setRetryAttempts(3)                // 재시도 횟수
				.setRetryInterval(1500)             // 재시도 간격 (ms)
				.setTimeout(3000)                   // 명령 실행 타임아웃 (ms)
				.setConnectTimeout(10000);          // 연결 타임아웃 (ms)

		return Redisson.create(config);
	}
}