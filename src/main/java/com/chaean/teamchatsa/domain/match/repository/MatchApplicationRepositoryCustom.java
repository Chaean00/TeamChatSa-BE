package com.chaean.teamchatsa.domain.match.repository;

import com.chaean.teamchatsa.domain.match.dto.response.MatchApplicantRes;

import java.util.List;

public interface MatchApplicationRepositoryCustom {

	/** 매치 신청 팀 목록 조회 (팀 정보 포함) */
	List<MatchApplicantRes> findApplicantsByMatchIdWithTeamInfo(Long matchId);
}
