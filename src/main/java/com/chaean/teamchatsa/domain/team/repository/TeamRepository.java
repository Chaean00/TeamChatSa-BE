package com.chaean.teamchatsa.domain.team.repository;

import com.chaean.teamchatsa.domain.team.dto.response.TeamListRes;
import com.chaean.teamchatsa.domain.team.model.Team;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long>, TeamRepositoryCustom {

    Optional<Team> findByIdAndIsDeletedFalse(Long teamId);

    boolean existsByIdAndIsDeletedFalse(Long teamId);

    boolean existsByLeaderUserIdAndIsDeletedFalse(Long userId);
}