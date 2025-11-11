package com.chaean.teamchatsa.domain.team.repository;

import com.chaean.teamchatsa.domain.team.model.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    boolean existsByUserIdAndIsDeletedFalse(Long userId);
    Long countByTeamIdAndIsDeletedFalse(Long teamId);
    Optional<TeamMember> findByTeamIdAndUserIdAndIsDeletedFalse(Long teamId, Long userId);
}