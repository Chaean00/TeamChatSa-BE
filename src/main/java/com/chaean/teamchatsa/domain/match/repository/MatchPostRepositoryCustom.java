package com.chaean.teamchatsa.domain.match.repository;

import com.chaean.teamchatsa.domain.match.dto.request.MatchPostSearchReq;
import com.chaean.teamchatsa.domain.match.dto.response.MatchPostDetailRes;
import com.chaean.teamchatsa.domain.match.dto.response.MatchPostListRes;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface MatchPostRepositoryCustom {

	/** MatchPost 목록 조회 (페이지네이션 + 필터링) */
	Slice<MatchPostListRes> findMatchPostsWithPagination(MatchPostSearchReq searchReq, Pageable pageable);

	/** 특정 팀의 MatchPost 목록 조회 (페이지네이션) */
	Slice<MatchPostListRes> findMatchPostsByTeamId(Long teamId, Pageable pageable);

	/** MatchPost 상세 조회 */
	MatchPostDetailRes findMatchPostDetailById(Long matchId);
}
