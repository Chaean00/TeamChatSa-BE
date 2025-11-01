package com.chaean.teamchatsa.domain.user.service;

import com.chaean.teamchatsa.domain.user.model.OAuthAccount;
import com.chaean.teamchatsa.domain.user.model.OAuthProvider;
import com.chaean.teamchatsa.domain.user.model.User;
import com.chaean.teamchatsa.domain.user.model.UserRole;
import com.chaean.teamchatsa.domain.user.repository.OAuthAccountRepository;
import com.chaean.teamchatsa.domain.user.repository.UserRepository;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import com.chaean.teamchatsa.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {
	private final UserRepository userRepo;
	private final OAuthAccountRepository oauthRepo;
	private final JwtProvider jwtProvider;
	private final PasswordEncoder encoder;

	public String loginByKakao(String kakaoId, String emailFromProvider, String nickname, String profileImg) {
		Optional<OAuthAccount> existing = oauthRepo.findByProviderAndProviderUserIdAndIsDeletedFalse(OAuthProvider.KAKAO, kakaoId);
		User user;

		if (existing.isPresent()) {
			OAuthAccount account = existing.get();
			Optional<User> userOpt = userRepo.findByIdAndIsDeletedFalse(account.getUserId());
			if (userOpt.isEmpty()) {
				throw new IllegalStateException("연결된 사용자 정보를 찾을 수 없습니다.");
			}
			user = userOpt.get();
			account.syncProfile(emailFromProvider, nickname, profileImg);
			// log
		} else {
			String username = nickname != null ? nickname : "kakao_" + kakaoId;
			String randomPassword = encoder.encode(UUID.randomUUID().toString());
			user = User.of(username, emailFromProvider, randomPassword, UserRole.PLAYER);
			userRepo.save(user);

			OAuthAccount link = OAuthAccount.kakaoLink(user.getId(), kakaoId, emailFromProvider, nickname, profileImg);
			oauthRepo.save(link);
		}

		return jwtProvider.createAccessToken(user.getId());
	}
}
