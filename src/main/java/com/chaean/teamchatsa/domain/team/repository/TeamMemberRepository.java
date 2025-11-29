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
    boolean existsByUserIdAndIsDeletedFalse(Long userId);

    Long countByTeamIdAndIsDeletedFalse(Long teamId);

    Optional<TeamMember> findByTeamIdAndUserIdAndIsDeletedFalse(Long teamId, Long userId);

    Optional<TeamMember> findByUserIdAndIsDeletedFalse(Long userId);

    List<TeamMember> findByTeamIdAndIsDeletedFalse(Long teamId);

    @Query("""
        SELECT tm.teamId
          FROM TeamMember tm
         WHERE tm.userId = :userId
           AND tm.isDeleted = false
    """)
    Long findTeamIdByUserIdAndIsDeletedFalse(Long userId);

    /** 특정 팀의 특정 역할을 가진 멤버 목록 조회 */
    @Query("""
        SELECT tm
          FROM TeamMember tm
         WHERE tm.teamId = :teamId
           AND tm.role IN :roles
           AND tm.isDeleted = false
    """)
    List<TeamMember> findByTeamIdAndRoleInAndIsDeletedFalse(@Param("teamId") Long teamId, @Param("roles") List<TeamRole> roles);
}