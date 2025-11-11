package com.chaean.teamchatsa.domain.user.service;

import com.chaean.teamchatsa.domain.user.dto.requset.PasswordUpdateReq;
import com.chaean.teamchatsa.domain.user.dto.requset.UserUpdateReq;
import com.chaean.teamchatsa.domain.user.dto.response.UserRes;
import com.chaean.teamchatsa.domain.user.model.User;
import com.chaean.teamchatsa.domain.user.repository.OAuthAccountRepository;
import com.chaean.teamchatsa.domain.user.repository.UserRepository;
import com.chaean.teamchatsa.global.common.aop.annotation.Loggable;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

	private final UserRepository userRepo;
	private final OAuthAccountRepository authRepo;
	private final PasswordEncoder encoder;

	@Transactional(readOnly = true)
	@Loggable
	public UserRes findUser(Long userId) {
		User user = userRepo.findByIdAndIsDeletedFalse(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "유저 정보를 찾을 수 없습니다."));

		boolean isLinked = authRepo.existsByUserIdAndIsDeletedFalse(user.getId());

		return UserRes.builder()
				.id(user.getId())
				.phone(user.getPhone())
				.name(user.getUsername())
				.position(user.getPosition())
				.email(user.getEmail())
				.nickname(user.getNickname())
				.isLocalAccount(!isLinked)
				.build();
	}

	@Transactional
	@Loggable
	public void updateUser(Long userId, UserUpdateReq req) {
		User user = userRepo.findByIdAndIsDeletedFalse(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "유저 정보를 찾을 수 없습니다."));

		user.update(req);
	}

	@Transactional(readOnly = true)
	@Loggable
	public boolean existsByNickname(String nickname) {
		Objects.requireNonNull(nickname);
		boolean exists = userRepo.existsByNicknameAndIsDeletedFalse(nickname.trim());

		if (exists) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 사용중인 닉네임입니다.");
		}
		return true;
	}

	@Transactional
	@Loggable
	public void updatePassword(Long userId, PasswordUpdateReq req) {
		if (req.newPassword().length() < 8) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "비밀번호는 8글자 이상이어야 합니다.");
		}

		User user = userRepo.findByIdAndIsDeletedFalse(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "유저 정보를 찾을 수 없습니다."));

		if (!encoder.matches(req.currentPassword(), user.getPassword())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "현재 비밀번호가 일치하지 않습니다.");
		}

		user.updatePassword(encoder.encode(req.newPassword()));
	}

	@Transactional
	@Loggable
	public void deleteUser(Long userId) {
		User user = userRepo.findByIdAndIsDeletedFalse(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "유저 정보를 찾을 수 없습니다."));

		user.softDelete();
	}
}
