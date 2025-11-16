package com.chaean.teamchatsa.domain.match.repository;

import com.chaean.teamchatsa.domain.match.dto.response.MatchPostDetailRes;
import com.chaean.teamchatsa.domain.match.dto.response.MatchPostListRes;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface MatchPostRepositoryCustom {

	/** MatchPost 목록 조회 (커서 기반 페이징) */
	Slice<MatchPostListRes> findMatchPostListWithPagination(Pageable pageable);

	/** 특정 팀의 MatchPost 목록 조회 (커서 기반 페이징) */
	Slice<MatchPostListRes> findMatchPostListByTeamId(Long teamId, Pageable pageable);

	MatchPostDetailRes findMatchPostDetailById(Long matchId);
}
