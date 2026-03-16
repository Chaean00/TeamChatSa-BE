package com.chaean.teamchatsa.domain.team.repository;

import com.chaean.teamchatsa.domain.team.dto.response.TeamMemberResponse;
import java.util.List;

public interface TeamMemberRepositoryCustom {

	List<TeamMemberResponse> findTeamMembersByTeamId(Long teamId);
}
