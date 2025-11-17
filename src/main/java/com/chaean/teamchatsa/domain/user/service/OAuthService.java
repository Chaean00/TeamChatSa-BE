package com.chaean.teamchatsa.domain.user.service;

import com.chaean.teamchatsa.domain.user.dto.response.TokenRes;
import com.chaean.teamchatsa.domain.user.model.OAuthAccount;
import com.chaean.teamchatsa.domain.user.model.OAuthProvider;
import com.chaean.teamchatsa.domain.user.model.User;
import com.chaean.teamchatsa.domain.user.model.UserRole;
import com.chaean.teamchatsa.domain.user.repository.OAuthAccountRepository;
import com.chaean.teamchatsa.domain.user.repository.UserRepository;
import com.chaean.teamchatsa.global.common.aop.annotation.Loggable;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import com.chaean.teamchatsa.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {
	private final UserRepository userRepo;
	private final OAuthAccountRepository oauthRepo;
	private final RedisTemplate<String, String> redisTemplate;
	private final JwtProvider jwtProvider;
	private final PasswordEncoder encoder;

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

		// 기존 사용자의 RefreshToken 무효화 (동시 로그인 방지)
		redisTemplate.delete("refresh:token:" + redisTemplate.opsForValue().get("refresh:user:" + user.getId()));
		redisTemplate.delete("refresh:user:" + user.getId());

		// RefreshToken 생성 및 Redis 저장
		String accessToken = jwtProvider.createAccessToken(user.getId());
		String refreshToken = jwtProvider.createRefreshToken(user.getId());
		redisTemplate.opsForValue().set(
				"refresh:token:" + refreshToken,
				user.getId().toString(),
				Duration.ofDays(14)
		);

		redisTemplate.opsForValue().set(
				"refresh:user:" + user.getId(),
				refreshToken,
				Duration.ofDays(14)
		);

		return new TokenRes(accessToken, refreshToken);
	}
}
