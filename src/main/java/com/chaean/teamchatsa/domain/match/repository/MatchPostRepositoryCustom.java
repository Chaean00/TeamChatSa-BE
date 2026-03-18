package com.chaean.teamchatsa.domain.match.repository;

import com.chaean.teamchatsa.domain.match.dto.request.MatchPostSearchRequest;
import com.chaean.teamchatsa.domain.match.dto.response.MatchPostDetailResponse;
import com.chaean.teamchatsa.domain.match.dto.response.MatchPostListResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface MatchPostRepositoryCustom {

	/**
	 * MatchPost 목록 조회 (페이지네이션 + 필터링)
	 */
	Slice<MatchPostListResponse> findMatchPostsWithPagination(MatchPostSearchRequest searchReq, Pageable pageable);

	/**
	 * 특정 팀의 MatchPost 목록 조회 (페이지네이션)
	 */
	Slice<MatchPostListResponse> findMatchPostsByTeamId(Long teamId, Pageable pageable);

	/**
	 * MatchPost 상세 조회
	 */
	MatchPostDetailResponse findMatchPostDetailById(Long matchId);
}
