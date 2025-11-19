package com.chaean.teamchatsa.domain.match.repository;

import com.chaean.teamchatsa.domain.match.model.MatchPost;
import com.chaean.teamchatsa.domain.match.repository.projection.MatchLocationProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MatchPostRepository extends JpaRepository<MatchPost, Long>, MatchPostRepositoryCustom {
	Optional<MatchPost> findByIdAndIsDeletedFalse(Long matchId);
	boolean existsByIdAndIsDeletedFalse(Long matchId);

	/**
	 * 위치 기반 매치 검색 (Native Query)
	 * PostGIS ST_DWithin으로 반경 내 필터링, KNN 연산자로 거리ㅇㅇㄴㅁㄹㄹㄴㅁㅇㅁㅁ순 정렬
	 */
	@Query(value = """
		/* language=SQL */
		SELECT
			mp.id AS id,
			mp.title AS title,
			mp.place_name AS placeName,
			mp.match_date AS matchDate,
			t.name AS teamName,
			mp.address AS address,
			mp.status AS status,
			t.level AS level,
		    mp.lat AS lat,
		    mp.lng AS lng,
		    ST_Distance(
			    mp.location::geography,
			    ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
		    ) AS distance
		FROM
			match_post mp
		LEFT JOIN
			team t
			ON t.id = mp.team_id AND t.is_deleted = false
		WHERE
			mp.is_deleted = false
		  	AND mp.status = 'OPEN'
		  	AND mp.match_date >= :currentDateTime
		  	AND ST_DWithin(
					mp.location::geography,
					ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
					:radius
				)
		ORDER BY
			mp.location <-> ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
		""",

		countQuery = """
		/* language=SQL */
		SELECT COUNT(*)
		FROM match_post mp
		WHERE
			mp.is_deleted = false
		  	AND mp.status = 'OPEN'
		  	AND mp.match_date >= :currentDateTime
		  	AND ST_DWithin(
					mp.location::geography,
					ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
					:radius
				)
		""",
	nativeQuery = true)
	Page<MatchLocationProjection> findMatchPostsByLocation(
			@Param("lat") Double lat,
			@Param("lng") Double lng,
			@Param("radius") Double radiusInMeters,
			@Param("currentDateTime") LocalDateTime currentDateTime,
			Pageable pageable
	);
}