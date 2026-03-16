package com.chaean.teamchatsa.infra.redis;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService {

	private final RedisTemplate<String, String> redisTemplate;
	@Value("${app.jwt.refresh-days}")
	private long refreshDays;

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
