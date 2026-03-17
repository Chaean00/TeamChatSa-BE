package com.chaean.teamchatsa.domain.team.repository;

import com.chaean.teamchatsa.domain.team.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamRepository extends JpaRepository<Team, Long>, TeamRepositoryCustom {
	
	@Query("""
			SELECT EXISTS (
			    SELECT 1
			      FROM Team t
			     WHERE t.name = :teamName
			)
			""")
	boolean existsByName(String teamName);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = """
			UPDATE app.team
			   SET style_vector = CAST(:styleVector AS vector)
			 WHERE id = :teamId
			""", nativeQuery = true)
	int updateStyleVector(@Param("teamId") Long teamId, @Param("styleVector") String styleVector);
}
