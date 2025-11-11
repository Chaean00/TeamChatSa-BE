package com.chaean.teamchatsa.domain.team.repository;

import com.chaean.teamchatsa.domain.team.dto.response.TeamListRes;
import com.chaean.teamchatsa.domain.team.model.Team;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByAndIsDeletedFalse(Long teamId);
    boolean existsByIdAndIsDeletedFalse(Long teamId);

    boolean existsByLeaderUserIdAndIsDeletedFalse(Long userId);

    @Query("""
        SELECT new com.chaean.teamchatsa.domain.team.dto.response.TeamListRes(
                t.id,
                t.name,
                t.area,
                t.img,
                t.description,
                coalesce(
                    cast((
                        select count(tm.id)
                        from TeamMember tm
                        where tm.teamId= t.id
                          and tm.isDeleted = false
                    ) as long),
                    0L
                )
            )
        FROM Team t
        WHERE t.isDeleted = false
        ORDER BY t.createdAt desc, t.id desc 
    """)
    Slice<TeamListRes> findTeamListSlice(Pageable pageable);
}