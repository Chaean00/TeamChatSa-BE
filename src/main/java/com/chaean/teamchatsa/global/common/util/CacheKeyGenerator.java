package com.chaean.teamchatsa.global.common.util;

import com.chaean.teamchatsa.domain.match.dto.request.MatchPostSearchReq;
import org.springframework.stereotype.Component;

/** Redis 캐시 키 생성 Class */
@Component
public class CacheKeyGenerator {

    private static final String MATCH_LIST_PREFIX = "match:list:";

    /**
     * 매치 목록 검색 캐시 키 생성
     * 자주 조회될 것으로 예상되는 데이터들만 캐싱
     */
    public String generateMatchListKey(MatchPostSearchReq req) {
        StringBuilder key = new StringBuilder(MATCH_LIST_PREFIX);

        key.append(req.getPage()).append(":").append(req.getSize());

        // headCount 필터 (있으면 추가)
        if (req.getHeadCount() != null) {
            key.append(":hc").append(req.getHeadCount());
        }

        // region 필터 (있으면 추가)
        if (req.getRegion() != null && !req.getRegion().isBlank()) {
            key.append(":rg").append(req.getRegion());
        }

        return key.toString();
    }

    /**
     * 캐싱 대상인지 판단
     * 1. 필터 X                         → 0-9 페이지
     * 2. 필터 headCount=11              → 0-4 페이지
     * 3. 필터 region=서울                → 0-4 페이지
     * 4. 필터 headCount=11 + region=서울 → 0-4 페이지
     */
    public boolean isCacheable(MatchPostSearchReq req) {
        // 날짜 필터가 있으면 캐싱 제외
        if (req.getStartDate() != null || req.getEndDate() != null) {
            return false;
        }

        boolean hasHeadCount = req.getHeadCount() != null;
        boolean hasRegion = req.getRegion() != null && !req.getRegion().isBlank();
        boolean isHeadCount11 = hasHeadCount && req.getHeadCount() == 11;
        boolean isRegionSeoul = hasRegion && "서울".equals(req.getRegion());

        // 필터 X → 0-9 페이지
        if (!hasHeadCount && !hasRegion) {
            return req.getPage() >= 0 && req.getPage() <= 9;
        }

        // 필터 headCount=11
        if (isHeadCount11 && !hasRegion) {
            return req.getPage() >= 0 && req.getPage() <= 4;
        }

        // 필터 region=서울
        if (!hasHeadCount && isRegionSeoul) {
            return req.getPage() >= 0 && req.getPage() <= 4;
        }

        // 필터 headCount=11 + region=서울
        if (isHeadCount11 && isRegionSeoul) {
            return req.getPage() >= 0 && req.getPage() <= 4;
        }

        return false;
    }

    /** 매치 목록 캐시 전체 무효화 패턴 */
    public String getMatchListInvalidationPattern() {
        return MATCH_LIST_PREFIX + "*";
    }
}
