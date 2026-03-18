package com.chaean.teamchatsa.domain.team.repository;

import com.chaean.teamchatsa.domain.team.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TeamRepository extends JpaRepository<Team, Long>, TeamRepositoryCustom {

	@Query("""
			SELECT EXISTS (
			    SELECT 1
			      FROM Team t
			     WHERE t.name = :teamName
			)
			""")
	boolean existsByName(String teamName);
}
