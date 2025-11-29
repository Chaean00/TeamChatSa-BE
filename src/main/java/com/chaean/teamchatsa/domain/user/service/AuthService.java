package com.chaean.teamchatsa.domain.user.service;

import com.chaean.teamchatsa.domain.user.dto.response.TokenRes;
import com.chaean.teamchatsa.global.common.aop.annotation.Loggable;
import com.chaean.teamchatsa.global.jwt.JwtProvider;
import com.chaean.teamchatsa.domain.user.dto.requset.LoginReq;
import com.chaean.teamchatsa.domain.user.dto.response.LoginRes;
import com.chaean.teamchatsa.domain.user.dto.requset.SignupReq;
import com.chaean.teamchatsa.domain.user.model.User;
import com.chaean.teamchatsa.domain.user.repository.UserRepository;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import com.chaean.teamchatsa.infra.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
	public void signup(SignupReq req) {
		Optional<User> existsUser = userRepo.findByEmail(req.getEmail());
		if (existsUser.isPresent() && existsUser.get().isDeleted()) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 탈퇴한 이메일입니다.");
		if (existsUser.isPresent()) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 존재하는 이메일입니다.");
		if (req.getPassword().length() < 8) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "비밀번호는 8글자 이상이어야 합니다.");

		if (req.getPhone() != null) {
			boolean existsPhone = userRepo.existsByPhoneAndIsDeletedFalse(req.getPhone());
			if (existsPhone) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 회원가입을 진행한 핸드폰 번호입니다.");
		}

		User user = User.builder()
				.username(req.getUserName())
				.email(req.getEmail())
				.password(encoder.encode(req.getPassword()))
				.position(req.getPosition())
				.phone(req.getPhone())
				.build();

		userRepo.save(user);
	}

	@Loggable
	public LoginRes login(LoginReq req) {
		Optional<User> user = userRepo.findByEmailAndIsDeletedFalse(req.getEmail());

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

		return new LoginRes(accessToken, refreshToken);
	}

	@Loggable
	public TokenRes reissueToken(String refreshToken) {
		// RT 검증
		Long userId = jwtProvider.parseUserId(refreshToken);

		User user = userRepo.findByIdAndIsDeletedFalse(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		// 기존 RT 삭제
		redisService.deleteRefreshToken(userId);

		// 새로운 AT, RT 발급
		String newAccessToken = jwtProvider.createAccessToken(user.getId());
		String newRefreshToken = jwtProvider.createRefreshToken(user.getId());

		redisService.setRefreshToken(newRefreshToken, user.getId());

		return new TokenRes(newAccessToken, newRefreshToken);
	}

	@Loggable
	public void logout(Long userId) {
		redisService.deleteRefreshToken(userId);
		log.info("사용자 로그아웃 완료: userId={}", userId);
	}
}
