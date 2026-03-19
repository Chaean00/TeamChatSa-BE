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
	 * 지도 중심 좌표 기준으로 가까운 marker를 최대 개수만큼 조회한다.
	 */
	@Query(value = """
			SELECT
				limited.id AS id,
				limited.title AS title,
				limited.match_date AS matchDate,
				t.name AS teamName,
				t.level AS level,
				limited.lat AS lat,
				limited.lng AS lng
			FROM
				(
					SELECT
						mp.id,
						mp.title,
						mp.match_date,
						mp.team_id,
						mp.lat,
						mp.lng,
						mp.location <-> ST_SetSRID(ST_MakePoint(:centerLng, :centerLat), 4326) AS distance_score
					FROM
						app.match_post mp
					WHERE
						mp.deleted_at IS NULL
						AND mp.status = 'OPEN'
						AND mp.location && ST_MakeEnvelope(:swLng, :swLat, :neLng, :neLat, 4326)
						AND mp.match_date >= :currentDateTime
						AND (CAST(:startDate AS date) IS NULL OR mp.match_date >= CAST(:startDate AS timestamp))
						AND (CAST(:endDate AS date) IS NULL OR mp.match_date < CAST(:endDate AS timestamp) + INTERVAL '1 day')
						AND (:headCount IS NULL OR mp.head_count = :headCount)
					ORDER BY
						mp.location <-> ST_SetSRID(ST_MakePoint(:centerLng, :centerLat), 4326) ASC,
						mp.match_date ASC,
						mp.id DESC
					LIMIT :limit
				) limited
			LEFT JOIN
				app.team t
			    ON t.id = limited.team_id
				   AND t.deleted_at IS NULL
			ORDER BY
				limited.distance_score ASC,
				limited.match_date ASC,
				limited.id DESC
			""", nativeQuery = true)
	List<MatchLocationProjection> findMatchMarkersByLocation(
			@Param("swLat") Double swLat,
			@Param("swLng") Double swLng,
			@Param("neLat") Double neLat,
			@Param("neLng") Double neLng,
			@Param("centerLat") Double centerLat,
			@Param("centerLng") Double centerLng,
			@Param("currentDateTime") LocalDateTime currentDateTime,
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate,
			@Param("headCount") Integer headCount,
			@Param("limit") Integer limit
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
				AND (:region IS NULL OR mp.region = :region)
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
