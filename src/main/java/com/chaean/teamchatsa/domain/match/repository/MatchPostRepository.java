package com.chaean.teamchatsa.domain.match.repository;

import com.chaean.teamchatsa.domain.match.model.MatchPost;
import com.chaean.teamchatsa.domain.match.repository.projection.MatchLocationProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchPostRepository extends JpaRepository<MatchPost, Long>, MatchPostRepositoryCustom {
	Optional<MatchPost> findByIdAndIsDeletedFalse(Long matchId);
	boolean existsByIdAndIsDeletedFalse(Long matchId);

	/**
	 * 지도 기반 매치 검색
	 * PostGIS 공간 인덱스 최적화: && 연산자 + ST_Intersects
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
			match_post mp
		LEFT JOIN
			team t
		    ON t.id = mp.team_id
			   AND t.is_deleted = false
		WHERE
			mp.is_deleted = false
			AND mp.status = 'OPEN'
			AND mp.location && ST_MakeEnvelope(:swLng, :swLat, :neLng, :neLat, 4326)
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
}