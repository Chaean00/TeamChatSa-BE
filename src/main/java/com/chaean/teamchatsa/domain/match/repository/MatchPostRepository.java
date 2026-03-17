package com.chaean.teamchatsa.domain.match.repository;

import com.chaean.teamchatsa.domain.match.model.MatchPost;
import com.chaean.teamchatsa.domain.match.repository.projection.MatchLocationProjection;
import com.chaean.teamchatsa.domain.match.repository.projection.MatchRecommendationProjection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatchPostRepository extends JpaRepository<MatchPost, Long>, MatchPostRepositoryCustom {

	/**
	 * 지도 기반 매치 검색 PostGIS 공간 인덱스 최적화: && 연산자 + ST_Intersects
	 */
	@Query(value = """
			SELECT
				mp.id AS id,
				mp.title AS title,
				mp.match_date AS matchDate,
				t.name AS teamName,
				t.level AS level,
				mp.lat AS lat,
				mp.lng AS lng
			FROM
				app.match_post mp
			LEFT JOIN
				app.team t
			    ON t.id = mp.team_id
				   AND t.deleted_at IS NULL
			WHERE
				mp.deleted_at IS NULL
				AND mp.status = 'OPEN'				AND mp.location && ST_MakeEnvelope(:swLng, :swLat, :neLng, :neLat, 4326)
				AND ST_Intersects(mp.location, ST_MakeEnvelope(:swLng, :swLat, :neLng, :neLat, 4326))
				AND mp.match_date >= :currentDateTime
				AND (CAST(:startDate AS date) IS NULL OR mp.match_date >= CAST(:startDate AS timestamp))
				AND (CAST(:endDate AS date) IS NULL OR mp.match_date < CAST(:endDate AS timestamp) + INTERVAL '1 day')
				AND (:headCount IS NULL OR mp.head_count = :headCount)
				AND (:region IS NULL OR mp.address LIKE CONCAT(:region, '%'))
			ORDER BY
				mp.match_date ASC, mp.id DESC
			""", nativeQuery = true)
	List<MatchLocationProjection> findMatchPostsByLocation(
			@Param("swLat") Double swLat,
			@Param("swLng") Double swLng,
			@Param("neLat") Double neLat,
			@Param("neLng") Double neLng,
			@Param("currentDateTime") LocalDateTime currentDateTime,
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate,
			@Param("headCount") Integer headCount,
			@Param("region") String region
	);

	@Query(value = """
			SELECT
				mp.id AS matchId,
				mp.title AS matchTitle,
				mp.place_name AS placeName,
				mp.match_date AS matchDateTime,
				t.id AS teamId,
				t.name AS teamName,
				mp.address AS matchAddress,
				t.level AS teamLevel
			FROM
				app.match_post mp
			JOIN
				app.team t
			    ON t.id = mp.team_id
			   AND t.deleted_at IS NULL
			WHERE
				mp.deleted_at IS NULL
				AND mp.team_id <> :myTeamId
				AND mp.status = 'OPEN'
				AND mp.match_date >= :currentDateTime
				AND t.style_vector IS NOT NULL
				AND t.level IN (:levels)
				AND t.win_rate BETWEEN :minWinRate AND :maxWinRate
				AND (:region IS NULL OR mp.address LIKE CONCAT(:region, '%'))
			ORDER BY
				t.style_vector <=> CAST(:queryVector AS vector),
				mp.match_date ASC,
				mp.id DESC
			LIMIT 10
			""", nativeQuery = true)
	List<MatchRecommendationProjection> findRecommendedMatches(
			@Param("myTeamId") Long myTeamId,
			@Param("queryVector") String queryVector,
			@Param("region") String region,
			@Param("levels") List<Integer> levels,
			@Param("minWinRate") double minWinRate,
			@Param("maxWinRate") double maxWinRate,
			@Param("currentDateTime") LocalDateTime currentDateTime
	);
}
