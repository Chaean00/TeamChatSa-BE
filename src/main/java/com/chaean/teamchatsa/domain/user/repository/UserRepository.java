package com.chaean.teamchatsa.domain.user.repository;

import com.chaean.teamchatsa.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmailAndIsDeletedFalse(String email);
	Optional<User> findByIdAndIsDeletedFalse(Long id);
	boolean existsByEmailAndIsDeletedFalse(String email);
}