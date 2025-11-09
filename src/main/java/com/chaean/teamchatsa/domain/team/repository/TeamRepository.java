package com.chaean.teamchatsa.domain.team.repository;

import com.chaean.teamchatsa.domain.team.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
  boolean existsByLeaderUserIdAndIsDeletedFalse(Long userId);
}