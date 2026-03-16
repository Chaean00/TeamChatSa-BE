package com.chaean.teamchatsa.domain.user.service;

import com.chaean.teamchatsa.domain.team.model.TeamMember;
import com.chaean.teamchatsa.domain.team.repository.TeamMemberRepository;
import com.chaean.teamchatsa.domain.user.dto.requset.PasswordUpdateRequest;
import com.chaean.teamchatsa.domain.user.dto.requset.UserUpdateRequest;
import com.chaean.teamchatsa.domain.user.dto.response.UserResponse;
import com.chaean.teamchatsa.domain.user.model.User;
import com.chaean.teamchatsa.domain.user.repository.OAuthAccountRepository;
import com.chaean.teamchatsa.domain.user.repository.UserRepository;
import com.chaean.teamchatsa.global.common.aop.annotation.Loggable;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

	private final UserRepository userRepo;
	private final TeamMemberRepository teamMemberRepo;
	private final OAuthAccountRepository authRepo;
	private final PasswordEncoder encoder;

	@Transactional(readOnly = true)
	@Loggable
	public UserResponse findUser(Long userId) {
		User user = userRepo.findById(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "유저 정보를 찾을 수 없습니다."));

		TeamMember teamMember = teamMemberRepo.findByUserId(userId).orElse(null);

		boolean isLinked = authRepo.existsByUserId(user.getId());

		return UserResponse.builder()
				.id(user.getId())
				.phone(user.getPhone())
				.name(user.getUsername())
				.position(user.getPosition())
				.email(user.getEmail())
				.nickname(user.getNickname())
				.isLocalAccount(!isLinked)
				.teamId(teamMember != null ? teamMember.getTeamId() : null)
				.teamRole(teamMember != null ? teamMember.getRole() : null)
				.build();
	}

	@Transactional
	@Loggable
	public void updateUser(Long userId, UserUpdateRequest req) {
		User user = userRepo.findById(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "유저 정보를 찾을 수 없습니다."));

		user.update(req);
	}

	@Transactional(readOnly = true)
	@Loggable
	public boolean existsByNickname(String nickname) {
		Objects.requireNonNull(nickname);
		boolean exists = userRepo.existsByNickname(nickname.trim());

		if (exists) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 사용중인 닉네임입니다.");
		}
		return true;
	}

	@Transactional
	@Loggable
	public void updatePassword(Long userId, PasswordUpdateRequest req) {
		if (req.getNewPassword().length() < 8) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "비밀번호는 8글자 이상이어야 합니다.");
		}

		User user = userRepo.findById(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "유저 정보를 찾을 수 없습니다."));

		if (!encoder.matches(req.getCurrentPassword(), user.getPassword())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "현재 비밀번호가 일치하지 않습니다.");
		}

		user.updatePassword(encoder.encode(req.getNewPassword()));
	}

	@Transactional
	@Loggable
	public void deleteUser(Long userId) {
		User user = userRepo.findById(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "유저 정보를 찾을 수 없습니다."));

		userRepo.delete(user);
	}
}
