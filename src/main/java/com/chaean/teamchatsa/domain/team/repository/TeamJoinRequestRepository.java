package com.chaean.teamchatsa.domain.team.repository;

import com.chaean.teamchatsa.domain.team.model.TeamApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamJoinRequestRepository extends JpaRepository<TeamApplication, Long> {
	boolean existsByTeamIdAndUserId(Long teamId, Long userId);
}