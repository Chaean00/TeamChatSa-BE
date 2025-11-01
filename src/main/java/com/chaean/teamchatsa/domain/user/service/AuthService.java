package com.chaean.teamchatsa.domain.user.service;

import com.chaean.teamchatsa.domain.user.model.UserRole;
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

	@Transactional
	public void signup(SignupReq req) {
		Optional<User> existsUser = userRepo.findByEmail(req.email());
		if (existsUser.isPresent() && existsUser.get().getIsDeleted()) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 탈퇴한 이메일입니다.");
		if (existsUser.isPresent()) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 존재하는 이메일입니다.");
		if (req.password().length() < 8) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "비밀번호는 8글자 이상이어야 합니다.");

		boolean existsPhone = userRepo.existsByPhoneAndIsDeletedFalse(req.phone());
		if (existsPhone) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 회원가입을 진행한 핸드폰 번호입니다.");

		User user = User.builder()
				.username(req.userName())
				.email(req.email())
				.password(encoder.encode(req.password()))
				.position(req.position())
				.role(UserRole.PLAYER)
				.phone(req.phone())
				.build();

		userRepo.save(user);
		log.info("[회원가입] userId = {}, role = {}", user.getId(), user.getRole());
	}

	@Transactional(readOnly = true)
	public LoginRes login(LoginReq req) {
		Optional<User> user = userRepo.findByEmailAndIsDeletedFalse(req.email());

		if (user.isEmpty() || !encoder.matches(req.password(), user.get().getPassword())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "잘못된 로그인 정보입니다.");
		}

		String accessToken = jwtProvider.createAccessToken(user.get().getId());

		log.info("[로그인] userId = {}, token = {}", user.get().getId(), accessToken);
		return new LoginRes(accessToken);
	}
}
