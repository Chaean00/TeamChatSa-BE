package com.chaean.teamchatsa.domain.team.repository;

import com.chaean.teamchatsa.domain.team.dto.response.TeamListResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface TeamRepositoryCustom {

	/**
	 * 팀 목록 조회 (커서 기반 페이징)
	 */
	Slice<TeamListResponse> findTeamListWithPagination(Pageable pageable);

	/**
	 * 팀 목록 조회 (이름 검색
	 */
	Slice<TeamListResponse> findTeamListByNameWithPagination(Pageable pageable, String teamName);
}
