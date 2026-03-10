package com.chaean.teamchatsa.domain.team.repository;

import com.chaean.teamchatsa.domain.team.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long>, TeamRepositoryCustom {

    boolean existsByLeaderUserId(Long userId);

    @Query("""
    SELECT EXISTS (
        SELECT 1
          FROM Team t
         WHERE t.name = :teamName
    )
    """)
    boolean existsByName(String teamName);
}