package com.chaean.teamchatsa.domain.match.repository;

import com.chaean.teamchatsa.domain.match.model.MatchApplication;
import com.chaean.teamchatsa.domain.match.model.MatchApplicationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchApplicationRepository extends JpaRepository<MatchApplication, Long>, MatchApplicationRepositoryCustom {

	Optional<MatchApplication> findByPostIdAndApplicantTeamId(Long matchId, Long teamId);

	List<MatchApplication> findAllByPostIdAndStatus(Long matchId, MatchApplicationStatus status);

	boolean existsByPostIdAndApplicantTeamId(Long matchId, Long teamId);

	boolean existsByPostIdAndStatus(Long matchId, MatchApplicationStatus status);
}