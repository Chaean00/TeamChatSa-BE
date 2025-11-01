package com.chaean.teamchatsa.domain.user.repository;

import com.chaean.teamchatsa.domain.user.model.OAuthAccount;
import com.chaean.teamchatsa.domain.user.model.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {
	Optional<OAuthAccount> findByProviderAndProviderUserIdAndIsDeletedFalse(OAuthProvider provider, String providerUserId);
}