package com.chaean.teamchatsa.domain.team.repository;

import com.chaean.teamchatsa.domain.team.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long>, TeamRepositoryCustom {

    Optional<Team> findByIdAndIsDeletedFalse(Long teamId);

    boolean existsByIdAndIsDeletedFalse(Long teamId);

    boolean existsByLeaderUserIdAndIsDeletedFalse(Long userId);

    @Query("""
    SELECT EXISTS (
        SELECT 1
          FROM Team t
         WHERE t.name = :teamName
           AND t.isDeleted = false
    )
    """)
    boolean existsByNameAndIsDeletedFalse(String teamName);
}