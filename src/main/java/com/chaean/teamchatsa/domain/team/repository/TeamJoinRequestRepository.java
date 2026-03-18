package com.chaean.teamchatsa.domain.team.repository;

import com.chaean.teamchatsa.domain.team.model.TeamApplication;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamJoinRequestRepository extends JpaRepository<TeamApplication, Long>, TeamJoinRequestRepositoryCustom {

	boolean existsByTeamIdAndUserId(Long teamId, Long userId);

	Optional<TeamApplication> findByIdAndTeamId(Long applicationId, Long teamId);

	Optional<TeamApplication> findByTeamIdAndUserId(Long teamId, Long userId);
}