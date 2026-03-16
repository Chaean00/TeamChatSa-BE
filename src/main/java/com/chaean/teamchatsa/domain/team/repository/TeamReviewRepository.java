package com.chaean.teamchatsa.domain.team.repository;

import com.chaean.teamchatsa.domain.team.model.TeamReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamReviewRepository extends JpaRepository<TeamReview, Long> {
    /**
 * Count TeamReview records associated with the specified team ID.
 *
 * @param teamId the ID of the team whose reviews will be counted
 * @return the number of TeamReview records for the given team ID
 */
long countByTeamId(Long teamId);
}
