package com.chaean.teamchatsa.domain.team.repository;

import com.chaean.teamchatsa.domain.team.dto.response.TeamMemberRes;

import java.util.List;

public interface TeamMemberRepositoryCustom {
	List<TeamMemberRes> findTeamMembersByTeamId(Long teamId);
}
