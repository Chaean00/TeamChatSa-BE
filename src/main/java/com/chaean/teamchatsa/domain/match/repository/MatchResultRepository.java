package com.chaean.teamchatsa.domain.match.repository;

import com.chaean.teamchatsa.domain.match.model.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {
    
    /**
     * Count total matches in which the specified team participated.
     *
     * @param teamId the ID of the team to match against homeTeamId or awayTeamId
     * @return the number of matches the team participated in
     */
    @Query("SELECT COUNT(m) FROM MatchResult m WHERE m.homeTeamId = :teamId OR m.awayTeamId = :teamId")
    long countTotalMatches(@Param("teamId") Long teamId);

    /**
 * Counts matches where the specified team is recorded as the winner.
 *
 * @param teamId the ID of the team whose wins to count
 * @return the number of matches won by the specified team
 */
    long countByWinnerTeamId(Long teamId);
}
