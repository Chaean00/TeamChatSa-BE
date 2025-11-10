package com.chaean.teamchatsa.domain.user.service;

import com.chaean.teamchatsa.domain.user.dto.response.TokenRes;
import com.chaean.teamchatsa.domain.user.model.UserRole;
import com.chaean.teamchatsa.global.common.aop.annotation.Loggable;
import com.chaean.teamchatsa.global.jwt.JwtProvider;
import com.chaean.teamchatsa.domain.user.dto.requset.LoginReq;
import com.chaean.teamchatsa.domain.user.dto.response.LoginRes;
import com.chaean.teamchatsa.domain.user.dto.requset.SignupReq;
import com.chaean.teamchatsa.domain.user.model.User;
import com.chaean.teamchatsa.domain.user.repository.UserRepository;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
	private final UserRepository userRepo;
	private final RedisTemplate<String, String> redisTemplate;
	private final PasswordEncoder encoder;
	private final JwtProvider jwtProvider;

	@Transactional
	@Loggable
	public void signup(SignupReq req) {
		Optional<User> existsUser = userRepo.findByEmail(req.email());
		if (existsUser.isPresent() && existsUser.get().isDeleted()) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 탈퇴한 이메일입니다.");
		if (existsUser.isPresent()) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 존재하는 이메일입니다.");
		if (req.password().length() < 8) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "비밀번호는 8글자 이상이어야 합니다.");

		if (req.phone() != null) {
			boolean existsPhone = userRepo.existsByPhoneAndIsDeletedFalse(req.phone());
			if (existsPhone) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 회원가입을 진행한 핸드폰 번호입니다.");
		}

		User user = User.builder()
				.username(req.userName())
				.email(req.email())
				.password(encoder.encode(req.password()))
				.position(req.position())
				.role(UserRole.ROLE_PLAYER)
				.phone(req.phone())
				.build();

		userRepo.save(user);
	}

	@Transactional
	@Loggable
	public LoginRes login(LoginReq req) {
		Optional<User> user = userRepo.findByEmailAndIsDeletedFalse(req.email());

		if (user.isEmpty() || !encoder.matches(req.password(), user.get().getPassword())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "잘못된 로그인 정보입니다.");
		}

		Long userId = user.get().getId();

		// 기존 사용자의 RefreshToken 무효화 (동시 로그인 방지)
		redisTemplate.delete("refresh:user:" + userId);

		// AT 및 RT 발급
		String accessToken = jwtProvider.createAccessToken(userId);
		String refreshTokenValue = jwtProvider.createRefreshToken(userId);

		// RT Redis 저장
		redisTemplate.opsForValue().set(
				"refresh:token:" + refreshTokenValue,
				userId.toString(),
				Duration.ofDays(14)
		);

		// 사용자별 RT 매핑 저장
		redisTemplate.opsForValue().set(
				"refresh:user:" + userId,
				refreshTokenValue,
				Duration.ofDays(14)
		);

		return new LoginRes(accessToken, refreshTokenValue);
	}

	@Loggable
	public TokenRes reissueToken(String refreshToken) {
		// RT 검증
		String userIdStr = redisTemplate.opsForValue().get("refresh:token:" + refreshToken);
		if (userIdStr == null) {
			throw new BusinessException(ErrorCode.INVALID_TOKEN, "유효하지 않거나 만료된 Refresh Token입니다.");
		}

		Long userId = Long.parseLong(userIdStr);

		User user = userRepo.findByIdAndIsDeletedFalse(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		// 기존 RT 삭제
		redisTemplate.delete("refresh:token:" + refreshToken);
		redisTemplate.delete("refresh:user:" + userId);

		// 새로운 AT, RT 발급
		String newAccessToken = jwtProvider.createAccessToken(user.getId());
		String newRefreshTokenValue = jwtProvider.createRefreshToken(user.getId());

		redisTemplate.opsForValue().set(
				"refresh:token:" + newRefreshTokenValue,
				userId.toString(),
				Duration.ofDays(14)
		);

		redisTemplate.opsForValue().set(
				"refresh:user:" + userId,
				newRefreshTokenValue,
				Duration.ofDays(14)
		);

		return new TokenRes(newAccessToken, newRefreshTokenValue);
	}

	@Loggable
	public void logout(Long userId) {
		String refreshToken = redisTemplate.opsForValue().get("refresh:user:" + userId);
		if (refreshToken != null) {
			redisTemplate.delete("refresh:token:" + refreshToken);
			redisTemplate.delete("refresh:user:" + userId);
		}
		log.info("사용자 로그아웃 완료: userId={}", userId);
	}
}
