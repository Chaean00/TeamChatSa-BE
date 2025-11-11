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


	@Test
	@DisplayName("유저 정보 조회 - 로컬 계정")
	public void findUser_success_localAccount() throws Exception{
	    //given
		User user = new User(1L, "테스터", "테스터_닉네임", "abc@naver.com", "010-1234-5678", "1234", UserRole.ROLE_PLAYER, Position.CB);
		when(userRepo.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(user));
		when(authRepo.existsByUserIdAndIsDeletedFalse(1L)).thenReturn(false);

	    //when
		UserRes result = userService.findUser(1L);

	    //then
		assertThat(result.id()).isEqualTo(1L);
		assertThat(result.name()).isEqualTo("테스터");
		verify(userRepo).findByIdAndIsDeletedFalse(1L);
		verify(authRepo).existsByUserIdAndIsDeletedFalse(1L);
	}
	@Test
	@DisplayName("유저 정보 조회 - 소셜 계정")
	public void findUser_success_socialAccount() throws Exception{
	    //given
		LocalDateTime now = LocalDateTime.now();
		User user = new User(2L, "테스터", "테스터_닉네임", "abcd@naver.com", "010-1234-5678", "1234", UserRole.ROLE_PLAYER, Position.CB);
		OAuthAccount oAuthAccount = new OAuthAccount(1L, 2L, "1234", "abcd@naver.com", "프로필_닉네임", "이미지URL", now, null, OAuthProvider.KAKAO);
		when(userRepo.findByIdAndIsDeletedFalse(2L)).thenReturn(Optional.of(user));
		when(authRepo.existsByUserIdAndIsDeletedFalse(2L)).thenReturn(true);

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
	@DisplayName("유저 정보 조회 - 유저 없음")
	public void findUser_fail_userNotFount() throws Exception{
	    //given
	    when(userRepo.findByIdAndIsDeletedFalse(99L)).thenReturn(Optional.empty());

	    //when
	    //then
		assertThatThrownBy(() -> userService.findUser(99L))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("유저 정보를 찾을 수 없습니다.");
		// 메서드가 호출되지 않아야 함
		verify(authRepo, never()).existsByUserIdAndIsDeletedFalse(anyLong());
	}

	@Test
	@DisplayName("유저 정보 업데이트 - 성공")
	public void updateUser_success() throws Exception{
	    //given
		Long userId = 1L;
		User user = new User();
		User spyUser = spy(user);
		when(userRepo.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(spyUser));
		UserUpdateReq req = new UserUpdateReq("새닉네임", Position.CM, "010-9999-8888");

	    //when
		userService.updateUser(userId, req);

	    //then
		verify(userRepo).findByIdAndIsDeletedFalse(userId);
		verify(spyUser).update(req);
		verifyNoMoreInteractions(userRepo);
	}

	@Test
	@DisplayName("유저 정보 업데이트 - 실패")
	public void updateUser_fail() throws Exception{
	    //given
		Long userId = 99L;
		when(userRepo.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.empty());
		UserUpdateReq req = new UserUpdateReq("새닉네임", Position.CM, "010-9999-8888");

	    //when
	    //then
		assertThatThrownBy(() -> userService.updateUser(userId, req))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("유저 정보를 찾을 수 없습니다.");
		verify(userRepo).findByIdAndIsDeletedFalse(userId);
		verifyNoMoreInteractions(userRepo);
	}

	@Test
	@DisplayName("닉네임 중복 확인 - 성공")
	public void existsByNickname_success() throws Exception{
	    //given
	    String nickname = "uniqueNickname     ";
		String trimmedNickname = nickname.trim();
		when(userRepo.existsByNicknameAndIsDeletedFalse(trimmedNickname)).thenReturn(false);

	    //when
		boolean result = userService.existsByNickname(nickname);

	    //then
		assertThat(result).isTrue();
		verify(userRepo).existsByNicknameAndIsDeletedFalse(trimmedNickname);
		verifyNoMoreInteractions(userRepo);
	}

	@Test
	@DisplayName("닉네임 중복 확인 - 실패(이미 존재하는 닉네임)")
	public void existsByNickname_fail_existsNickname() throws Exception{
	    //given
	    String nickname = "existingNickname";
		when(userRepo.existsByNicknameAndIsDeletedFalse(nickname)).thenReturn(true);

	    //when
	    //then
		assertThatThrownBy(() -> userService.existsByNickname(nickname))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("이미 사용중인 닉네임입니다.");

		verify(userRepo).existsByNicknameAndIsDeletedFalse(argThat(s -> s.equals(s.trim())));
		verifyNoMoreInteractions(userRepo);
	}

	@Test
	@DisplayName("닉네임 중복 확인 - 실패(null값)")
	public void existsByNickname_fail_nullValue() throws Exception{
	    //given
	    String nickname = null;

	    //when
	    //then
		assertThatThrownBy(() -> userService.existsByNickname(nickname))
				.isInstanceOf(NullPointerException.class);

		verifyNoMoreInteractions(userRepo);
	}

	@Test
	@DisplayName("유저 비밀번호 변경 - 성공")
	public void updatePassword_success() throws Exception{
	    //given
		User user = User.builder()
				.id(1L)
				.password("123456789")
				.build();
		User spyUser = spy(user);

		PasswordUpdateReq req = new PasswordUpdateReq("currentPassword", "1111111111");

		when(userRepo.findByIdAndIsDeletedFalse(user.getId())).thenReturn(Optional.of(spyUser));
		when(encoder.matches("currentPassword", "123456789")).thenReturn(true);
		when(encoder.encode("1111111111")).thenReturn("encodedNewPassword");

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
	@DisplayName("유저 비밀번호 변경 - 실패(비밀번호 8글자 미만)")
	public void updatePassword_fail_shortPassword() throws Exception{
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
	@DisplayName("유저 비밀번호 변경 - 실패(비밀번호 일치 X)")
	public void updatePassword_fail_notEqualPassword() throws Exception{
	    //given
		User user = User.builder()
				.id(1L)
				.password("existingPassword")
				.build();
		PasswordUpdateReq req = new PasswordUpdateReq("wrongCur", "newTooLongPassword");
		when(encoder.matches("wrongCur", "existingPassword")).thenReturn(false);
		when(userRepo.findByIdAndIsDeletedFalse(user.getId())).thenReturn(Optional.of(user));

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
	@DisplayName("유저 비밀번호 변경 - 실패(유저 없음)")
	public void updatePassword_fail_userNotFound() throws Exception{
	    //given
		Long userId = 99L;
		PasswordUpdateReq req = new PasswordUpdateReq("anyCur", "newValidPassword");
		when(userRepo.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.empty());

	    //when
	    //then
		assertThatThrownBy(() -> userService.updatePassword(userId, req))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("유저 정보를 찾을 수 없습니다.");
		verify(userRepo).findByIdAndIsDeletedFalse(userId);
		verifyNoMoreInteractions(userRepo, encoder);
	}

	@Test
	@DisplayName("유저 삭제 - 성공")
	public void deleteUser_success() throws Exception{
	    //given
		User user = new User();
		User spyUser = spy(user);
		Long userId = 1L;
		when(userRepo.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(spyUser));

	    //when
		userService.deleteUser(userId);

	    //then
		verify(userRepo).findByIdAndIsDeletedFalse(userId);
		verify(spyUser).softDelete();
		assertThat(spyUser.isDeleted()).isTrue();
		verifyNoMoreInteractions(userRepo);
	}

	@Test
	@DisplayName("유저 삭제 - 실패(유저 없음)")
	public void deleteUser_fail_userNotFound() throws Exception{
	    //given
		Long userId = 99L;
		when(userRepo.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.empty());

	    //when
	    //then
		assertThatThrownBy(() -> userService.deleteUser(userId))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("유저 정보를 찾을 수 없습니다.");

		verify(userRepo).findByIdAndIsDeletedFalse(userId);
		verifyNoMoreInteractions(userRepo);
	}
}