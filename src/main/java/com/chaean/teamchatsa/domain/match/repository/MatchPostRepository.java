package com.chaean.teamchatsa.domain.match.repository;

import com.chaean.teamchatsa.domain.match.model.MatchPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchPostRepository extends JpaRepository<MatchPost, Long>, MatchPostRepositoryCustom {
	Optional<MatchPost> findByIdAndIsDeletedFalse(Long matchId);
	boolean existsByIdAndIsDeletedFalse(Long matchId);
}