package com.chaean.teamchatsa.domain.team.repository;

import com.chaean.teamchatsa.domain.team.dto.response.TeamMemberRes;
import com.chaean.teamchatsa.domain.team.model.TeamMember;
import com.chaean.teamchatsa.domain.team.model.TeamRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long>, TeamMemberRepositoryCustom {
    boolean existsByUserId(Long userId);

    Long countByTeamId(Long teamId);

    Optional<TeamMember> findByTeamIdAndUserId(Long teamId, Long userId);

    Optional<TeamMember> findByUserId(Long userId);

    List<TeamMember> findByTeamId(Long teamId);

    @Query("""
        SELECT tm.teamId
          FROM TeamMember tm
         WHERE tm.userId = :userId
    """)
    Long findTeamIdByUserId(Long userId);

    @Query("""
        SELECT tm
          FROM TeamMember tm
         WHERE tm.teamId = :teamId
           AND tm.role IN :roles
    """)
    List<TeamMember> findByTeamIdAndRoleIn(@Param("teamId") Long teamId, @Param("roles") List<TeamRole> roles);
}