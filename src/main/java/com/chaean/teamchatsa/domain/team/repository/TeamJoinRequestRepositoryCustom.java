package com.chaean.teamchatsa.domain.team.repository;

import com.chaean.teamchatsa.domain.team.dto.response.TeamApplicationRes;
import com.chaean.teamchatsa.domain.team.model.JoinStatus;
import com.chaean.teamchatsa.domain.team.model.TeamApplication;

import java.util.List;

public interface TeamJoinRequestRepositoryCustom {

	/** 특정 팀의 가입 신청 목록 조회 (유저 정보 포함) */
	List<TeamApplicationRes> findApplicationsByTeamId(Long teamId);

	/** 특정 팀의 특정 상태 가입 신청 목록 조회 */
	List<TeamApplicationRes> findApplicationsByTeamIdAndStatus(Long teamId, JoinStatus status);

	/** 특정 사용자의 모든 PENDING 상태 가입 신청 조회 (특정 신청 제외) */
	List<TeamApplication> findPendingApplicationsByUserIdExcluding(Long userId, Long excludeApplicationId);
}