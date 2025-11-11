package com.chaean.teamchatsa.domain.user.service;

import com.chaean.teamchatsa.domain.team.model.Position;
import com.chaean.teamchatsa.domain.user.dto.requset.PasswordUpdateReq;
import com.chaean.teamchatsa.domain.user.dto.requset.UserUpdateReq;
import com.chaean.teamchatsa.domain.user.dto.response.UserRes;
import com.chaean.teamchatsa.domain.user.model.OAuthAccount;
import com.chaean.teamchatsa.domain.user.model.OAuthProvider;
import com.chaean.teamchatsa.domain.user.model.User;
import com.chaean.teamchatsa.domain.user.model.UserRole;
import com.chaean.teamchatsa.domain.user.repository.OAuthAccountRepository;
import com.chaean.teamchatsa.domain.user.repository.UserRepository;
import com.chaean.teamchatsa.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepo;
	@Mock
	private OAuthAccountRepository authRepo;
	@Mock
	private PasswordEncoder encoder;
	@InjectMocks
	private UserService userService;

	@Nested
	@DisplayName("유저 정보 조회")
	class FindUser {
		@Test
		@DisplayName("성공 - 로컬 계정")
		public void success_localAccount() {
			//given
			User user = new User(1L, "테스터", "테스터_닉네임", "abc@naver.com", "010-1234-5678", "1234", UserRole.USER, Position.CB);
			given(userRepo.findByIdAndIsDeletedFalse(1L)).willReturn(Optional.of(user));
			given(authRepo.existsByUserIdAndIsDeletedFalse(1L)).willReturn(false);

			//when
			UserRes result = userService.findUser(1L);

			//then
			assertThat(result.id()).isEqualTo(1L);
			assertThat(result.name()).isEqualTo("테스터");
			verify(userRepo).findByIdAndIsDeletedFalse(1L);
			verify(authRepo).existsByUserIdAndIsDeletedFalse(1L);
		}

		@Test
		@DisplayName("성공 - 소셜 계정")
		public void success_socialAccount() {
			//given
			LocalDateTime now = LocalDateTime.now();
			User user = new User(2L, "테스터", "테스터_닉네임", "abcd@naver.com", "010-1234-5678", "1234", UserRole.USER, Position.CB);
			OAuthAccount oAuthAccount = new OAuthAccount(1L, 2L, "1234", "abcd@naver.com", "프로필_닉네임", "이미지URL", now, null, OAuthProvider.KAKAO);
			given(userRepo.findByIdAndIsDeletedFalse(2L)).willReturn(Optional.of(user));
			given(authRepo.existsByUserIdAndIsDeletedFalse(2L)).willReturn(true);

			//when
			UserRes result = userService.findUser(2L);

			//then
			assertThat(oAuthAccount.getUserId()).isEqualTo(result.id());
			assertThat(result.isLocalAccount()).isFalse();
			assertThat(result.id()).isEqualTo(2L);
			assertThat(result.name()).isEqualTo("테스터");
			verify(userRepo).findByIdAndIsDeletedFalse(2L);
			verify(authRepo).existsByUserIdAndIsDeletedFalse(2L);
		}

		@Test
		@DisplayName("실패 - 유저 없음")
		public void fail_userNotFound() {
			//given
			given(userRepo.findByIdAndIsDeletedFalse(99L)).willReturn(Optional.empty());

			//when
			//then
			assertThatThrownBy(() -> userService.findUser(99L))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("유저 정보를 찾을 수 없습니다.");
			// 메서드가 호출되지 않아야 함
			verify(authRepo, never()).existsByUserIdAndIsDeletedFalse(anyLong());
		}
	}

	@Nested
	@DisplayName("유저 정보 업데이트")
	class UpdateUser {
		@Test
		@DisplayName("성공")
		public void success() {
			//given
			Long userId = 1L;
			User user = new User();
			User spyUser = spy(user);
			given(userRepo.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.of(spyUser));
			UserUpdateReq req = new UserUpdateReq("새닉네임", Position.CM, "010-9999-8888");

			//when
			userService.updateUser(userId, req);

			//then
			verify(userRepo).findByIdAndIsDeletedFalse(userId);
			verify(spyUser).update(req);
			verifyNoMoreInteractions(userRepo);
		}

		@Test
		@DisplayName("실패 - 유저 없음")
		public void fail_userNotFound() {
			//given
			Long userId = 99L;
			given(userRepo.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.empty());
			UserUpdateReq req = new UserUpdateReq("새닉네임", Position.CM, "010-9999-8888");

			//when
			//then
			assertThatThrownBy(() -> userService.updateUser(userId, req))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("유저 정보를 찾을 수 없습니다.");
			verify(userRepo).findByIdAndIsDeletedFalse(userId);
			verifyNoMoreInteractions(userRepo);
		}
	}

	@Nested
	@DisplayName("닉네임 중복 확인")
	class ExistsByNickname {
		@Test
		@DisplayName("성공")
		public void success() {
			//given
			String nickname = "uniqueNickname     ";
			String trimmedNickname = nickname.trim();
			given(userRepo.existsByNicknameAndIsDeletedFalse(trimmedNickname)).willReturn(false);

			//when
			boolean result = userService.existsByNickname(nickname);

			//then
			assertThat(result).isTrue();
			verify(userRepo).existsByNicknameAndIsDeletedFalse(trimmedNickname);
			verifyNoMoreInteractions(userRepo);
		}

		@Test
		@DisplayName("실패 - 이미 존재하는 닉네임")
		public void fail_existsNickname() {
			//given
			String nickname = "existingNickname";
			given(userRepo.existsByNicknameAndIsDeletedFalse(nickname)).willReturn(true);

			//when
			//then
			assertThatThrownBy(() -> userService.existsByNickname(nickname))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("이미 사용중인 닉네임입니다.");

			verify(userRepo).existsByNicknameAndIsDeletedFalse(argThat(s -> s.equals(s.trim())));
			verifyNoMoreInteractions(userRepo);
		}

		@Test
		@DisplayName("실패 - null값")
		public void fail_nullValue() {
			//given
			String nickname = null;

			//when
			//then
			assertThatThrownBy(() -> userService.existsByNickname(nickname))
				.isInstanceOf(NullPointerException.class);

			verifyNoMoreInteractions(userRepo);
		}
	}

	@Nested
	@DisplayName("유저 비밀번호 변경")
	class UpdatePassword {
		@Test
		@DisplayName("성공")
		public void success() {
			//given
			User user = User.builder()
				.id(1L)
				.password("123456789")
				.build();
			User spyUser = spy(user);

			PasswordUpdateReq req = new PasswordUpdateReq("currentPassword", "1111111111");

			given(userRepo.findByIdAndIsDeletedFalse(user.getId())).willReturn(Optional.of(spyUser));
			given(encoder.matches("currentPassword", "123456789")).willReturn(true);
			given(encoder.encode("1111111111")).willReturn("encodedNewPassword");

			//when
			userService.updatePassword(user.getId(), req);

			//then
			verify(userRepo).findByIdAndIsDeletedFalse(user.getId());
			verify(spyUser).getPassword();
			verify(encoder).matches("currentPassword", "123456789");
			verify(encoder).encode("1111111111");
			verify(spyUser).updatePassword("encodedNewPassword");
			verifyNoMoreInteractions(userRepo, encoder, spyUser);
		}

		@Test
		@DisplayName("실패 - 비밀번호 8글자 미만")
		public void fail_shortPassword() {
			//given
			Long userId = 1L;
			PasswordUpdateReq req = new PasswordUpdateReq("curr", "short");

			//when
			//then
			assertThatThrownBy(() -> userService.updatePassword(userId, req))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("비밀번호는 8글자 이상이어야 합니다.");

			verifyNoMoreInteractions(userRepo, encoder);
		}

		@Test
		@DisplayName("실패 - 비밀번호 일치 X")
		public void fail_notEqualPassword() {
			//given
			User user = User.builder()
				.id(1L)
				.password("existingPassword")
				.build();
			PasswordUpdateReq req = new PasswordUpdateReq("wrongCur", "newTooLongPassword");
			given(encoder.matches("wrongCur", "existingPassword")).willReturn(false);
			given(userRepo.findByIdAndIsDeletedFalse(user.getId())).willReturn(Optional.of(user));

			//when
			//then
			assertThatThrownBy(() -> userService.updatePassword(user.getId(), req))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("현재 비밀번호가 일치하지 않습니다.");
			verify(userRepo).findByIdAndIsDeletedFalse(user.getId());
			verify(encoder).matches("wrongCur", "existingPassword");
			verifyNoMoreInteractions(userRepo, encoder);
		}

		@Test
		@DisplayName("실패 - 유저 없음")
		public void fail_userNotFound() {
			//given
			Long userId = 99L;
			PasswordUpdateReq req = new PasswordUpdateReq("anyCur", "newValidPassword");
			given(userRepo.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.empty());

			//when
			//then
			assertThatThrownBy(() -> userService.updatePassword(userId, req))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("유저 정보를 찾을 수 없습니다.");
			verify(userRepo).findByIdAndIsDeletedFalse(userId);
			verifyNoMoreInteractions(userRepo, encoder);
		}
	}

	@Nested
	@DisplayName("유저 삭제")
	class DeleteUser {
		@Test
		@DisplayName("성공")
		public void success() {
			//given
			User user = new User();
			User spyUser = spy(user);
			Long userId = 1L;
			given(userRepo.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.of(spyUser));

			//when
			userService.deleteUser(userId);

			//then
			verify(userRepo).findByIdAndIsDeletedFalse(userId);
			verify(spyUser).softDelete();
			assertThat(spyUser.isDeleted()).isTrue();
			verifyNoMoreInteractions(userRepo);
		}

		@Test
		@DisplayName("실패 - 유저 없음")
		public void fail_userNotFound() {
			//given
			Long userId = 99L;
			given(userRepo.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.empty());

			//when
			//then
			assertThatThrownBy(() -> userService.deleteUser(userId))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("유저 정보를 찾을 수 없습니다.");

			verify(userRepo).findByIdAndIsDeletedFalse(userId);
			verifyNoMoreInteractions(userRepo);
		}
	}
}