package com.chaean.teamchatsa.domain.user.service;

import com.chaean.teamchatsa.domain.user.dto.requset.LoginRequest;
import com.chaean.teamchatsa.domain.user.dto.requset.SignupRequest;
import com.chaean.teamchatsa.domain.user.dto.response.LoginResponse;
import com.chaean.teamchatsa.domain.user.dto.response.TokenResponse;
import com.chaean.teamchatsa.domain.user.model.User;
import com.chaean.teamchatsa.domain.user.repository.UserRepository;
import com.chaean.teamchatsa.global.common.aop.annotation.Loggable;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import com.chaean.teamchatsa.global.jwt.JwtProvider;
import com.chaean.teamchatsa.infra.redis.RedisService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepo;
	private final PasswordEncoder encoder;
	private final JwtProvider jwtProvider;
	private final RedisService redisService;

	@Transactional
	@Loggable
	public void signup(SignupRequest req) {
		Optional<User> existsUser = userRepo.findByEmail(req.getEmail());
		if (existsUser.isPresent() && existsUser.get().isDeleted()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 탈퇴한 이메일입니다.");
		}
		if (existsUser.isPresent()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 존재하는 이메일입니다.");
		}
		if (req.getPassword().length() < 8) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "비밀번호는 8글자 이상이어야 합니다.");
		}

		if (req.getPhone() != null) {
			boolean existsPhone = userRepo.existsByPhone(req.getPhone());
			if (existsPhone) {
				throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 회원가입을 진행한 핸드폰 번호입니다.");
			}
		}

		User user = User.create(
				req.getUserName(),
				req.getEmail(),
				encoder.encode(req.getPassword()),
				req.getPosition(),
				req.getPhone()
		);

		userRepo.save(user);
	}

	@Loggable
	public LoginResponse login(LoginRequest req) {
		Optional<User> user = userRepo.findByEmail(req.getEmail());

		if (user.isEmpty() || !encoder.matches(req.getPassword(), user.get().getPassword())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "잘못된 로그인 정보입니다.");
		}

		Long userId = user.get().getId();

		// 기존 RT 삭제
		redisService.deleteRefreshToken(userId);

		// AT 및 RT 발급
		String accessToken = jwtProvider.createAccessToken(userId);
		String refreshToken = jwtProvider.createRefreshToken(userId);

		// 사용자별 RT 매핑 저장
		redisService.setRefreshToken(refreshToken, userId);

		return new LoginResponse(accessToken, refreshToken);
	}

	@Loggable
	public TokenResponse reissueToken(String refreshToken) {
		// RT 검증
		Long userId = jwtProvider.parseUserId(refreshToken);

		User user = userRepo.findById(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		// 기존 RT 삭제
		redisService.deleteRefreshToken(userId);

		// 새로운 AT, RT 발급
		String newAccessToken = jwtProvider.createAccessToken(user.getId());
		String newRefreshToken = jwtProvider.createRefreshToken(user.getId());

		redisService.setRefreshToken(newRefreshToken, user.getId());

		return new TokenResponse(newAccessToken, newRefreshToken);
	}

	@Loggable
	public void logout(Long userId) {
		redisService.deleteRefreshToken(userId);
		log.info("사용자 로그아웃 완료: userId={}", userId);
	}
}
