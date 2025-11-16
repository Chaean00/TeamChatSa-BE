package com.chaean.teamchatsa.domain.team.repository;

import com.chaean.teamchatsa.domain.team.dto.response.TeamMemberRes;
import com.chaean.teamchatsa.domain.team.model.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long>, TeamMemberRepositoryCustom {
    boolean existsByUserIdAndIsDeletedFalse(Long userId);

    Long countByTeamIdAndIsDeletedFalse(Long teamId);

    Optional<TeamMember> findByTeamIdAndUserIdAndIsDeletedFalse(Long teamId, Long userId);

    Optional<TeamMember> findByUserIdAndIsDeletedFalse(Long userId);

    @Query("""
        SELECT tm.teamId
          FROM TeamMember tm
         WHERE tm.userId = :userId
           AND tm.isDeleted = false
    """)
    Long findTeamIdByUserIdAndIsDeletedFalse(Long userId);
}