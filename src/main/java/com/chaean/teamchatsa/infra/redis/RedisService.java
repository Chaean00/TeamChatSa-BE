package com.chaean.teamchatsa.infra.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {
	@Value("${app.jwt.refresh-days}")
	private long refreshDays;
	private final RedisTemplate<String, String> redisTemplate;

	public void setRefreshToken(String refreshToken, Long userId) {
		redisTemplate.opsForValue().set(
				"refresh:user:" + userId,
				refreshToken,
				Duration.ofDays(refreshDays)
		);
	}

	public void deleteRefreshToken(Long userId) {
		redisTemplate.delete("refresh:user:" + userId);
	}
}
