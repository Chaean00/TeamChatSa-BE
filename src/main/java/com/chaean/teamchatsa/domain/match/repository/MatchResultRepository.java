package com.chaean.teamchatsa.domain.match.repository;

import com.chaean.teamchatsa.domain.match.model.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {
    
    // 해당 팀이 참여한 총 경기 수
    @Query("SELECT COUNT(m) FROM MatchResult m WHERE m.homeTeamId = :teamId OR m.awayTeamId = :teamId")
    long countTotalMatches(@Param("teamId") Long teamId);

    // 해당 팀이 승리한 경기 수
    long countByWinnerTeamId(Long teamId);
}
