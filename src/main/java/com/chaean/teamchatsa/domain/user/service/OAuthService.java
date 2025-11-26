package com.chaean.teamchatsa.domain.user.service;

import com.chaean.teamchatsa.domain.user.dto.response.TokenRes;
import com.chaean.teamchatsa.domain.user.model.OAuthAccount;
import com.chaean.teamchatsa.domain.user.model.OAuthProvider;
import com.chaean.teamchatsa.domain.user.model.User;
import com.chaean.teamchatsa.domain.user.repository.OAuthAccountRepository;
import com.chaean.teamchatsa.domain.user.repository.UserRepository;
import com.chaean.teamchatsa.global.common.aop.annotation.Loggable;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import com.chaean.teamchatsa.global.jwt.JwtProvider;
import com.chaean.teamchatsa.infra.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	private final RedisService redisService;

	@Transactional
	@Loggable
	public TokenRes loginByKakao(String kakaoId, String emailFromProvider, String nickname, String profileImg) {
		Optional<OAuthAccount> existing = oauthRepo.findByProviderAndProviderUserIdAndIsDeletedFalse(OAuthProvider.KAKAO, kakaoId);
		User user;

		if (existing.isPresent()) {
			OAuthAccount account = existing.get();
			Optional<User> userOpt = userRepo.findByIdAndIsDeletedFalse(account.getUserId());
			if (userOpt.isEmpty()) {
				throw new BusinessException(ErrorCode.USER_NOT_FOUND);
			}
			user = userOpt.get();
			account.syncProfile(emailFromProvider, nickname, profileImg);
		} else {
			String username = nickname != null ? nickname : "kakao_" + kakaoId;
			String randomPassword = encoder.encode(UUID.randomUUID().toString());
			user = User.of(username, emailFromProvider, randomPassword);
			userRepo.save(user);

			OAuthAccount link = OAuthAccount.kakaoLink(user.getId(), kakaoId, emailFromProvider, nickname, profileImg);
			oauthRepo.save(link);
		}

		redisService.deleteRefreshToken(user.getId());

		// RefreshToken 생성 및 Redis 저장
		String accessToken = jwtProvider.createAccessToken(user.getId());
		String refreshToken = jwtProvider.createRefreshToken(user.getId());

		redisService.setRefreshToken(refreshToken, user.getId());

		return new TokenRes(accessToken, refreshToken);
	}
}
