package com.chaean.teamchatsa.domain.team.repository;

import com.chaean.teamchatsa.domain.team.model.TeamReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamReviewRepository extends JpaRepository<TeamReview, Long> {
    long countByTeamId(Long teamId);
}
