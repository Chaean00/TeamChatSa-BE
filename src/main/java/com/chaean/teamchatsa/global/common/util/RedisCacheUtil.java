package com.chaean.teamchatsa.global.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

/** Redis 캐싱 유틸리티 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisCacheUtil {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    /** 캐시 저장 */
    public <T> void set(String key, T value, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, ttl);
            log.debug("Cache SET: key={}, ttl={}s", key, ttl.getSeconds());
        } catch (Exception e) {
            log.error("캐시 저장 실패: key={}", key, e);
        }
    }

    /** 캐시 조회 TypeReference */
    public <T> T get(String key, TypeReference<T> typeReference) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) {
                log.debug("Cache MISS: key={}", key);
                return null;
            }
            log.debug("Cache HIT: key={}", key);
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            log.error("캐시 조회 실패: key={}", key, e);
            return null;
        }
    }

    /** 캐시 조회- Class 사용 */
    public <T> T get(String key, Class<T> clazz) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) {
                log.debug("Cache MISS: key={}", key);
                return null;
            }
            log.debug("Cache HIT: key={}", key);
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("캐시 조회 실패: key={}", key, e);
            return null;
        }
    }

    /** 캐시 삭제 */
    public void delete(String key) {
        redisTemplate.delete(key);
        log.debug("Cache DELETE: key={}", key);
    }

    /** 패턴 매칭으로 캐시 삭제 */
    public void deleteByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Cache DELETE by pattern: pattern={}, count={}", pattern, keys.size());
        }
    }

    /** 캐시 존재 여부 확인 */
    public boolean exists(String key) {
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists;
    }
}
