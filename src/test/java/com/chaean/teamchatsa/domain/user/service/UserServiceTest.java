package com.chaean.teamchatsa.domain.user.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.chaean.teamchatsa.domain.team.model.TeamMember;
import com.chaean.teamchatsa.domain.team.repository.TeamMemberRepository;
import com.chaean.teamchatsa.domain.user.dto.requset.PasswordUpdateRequest;
import com.chaean.teamchatsa.domain.user.dto.requset.UserUpdateRequest;
import com.chaean.teamchatsa.domain.user.dto.response.UserResponse;
import com.chaean.teamchatsa.domain.user.model.User;
import com.chaean.teamchatsa.domain.user.repository.OAuthAccountRepository;
import com.chaean.teamchatsa.domain.user.repository.UserRepository;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	private final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.build();
	@Mock
	private UserRepository userRepo;
	@Mock
	private TeamMemberRepository teamMemberRepo;
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
			User user = fixtureMonkey.giveMeOne(User.class);
			given(userRepo.findById(user.getId())).willReturn(Optional.of(user));
			given(teamMemberRepo.findByUserId(user.getId())).willReturn(Optional.empty());
			given(authRepo.existsByUserId(user.getId())).willReturn(false);

			//when
			UserResponse result = userService.findUser(user.getId());

			//then
			assertThat(result.getId()).isEqualTo(user.getId());
			assertThat(result.getName()).isEqualTo(user.getUsername());
			assertThat(result.getTeamId()).isNull();
			assertThat(result.getTeamRole()).isNull();
			verify(userRepo).findById(user.getId());
			verify(teamMemberRepo).findByUserId(user.getId());
			verify(authRepo).existsByUserId(user.getId());
		}

		@Test
		@DisplayName("성공 - 소셜 계정")
		public void success_socialAccount() {
			//given
			User user = fixtureMonkey.giveMeOne(User.class);
			TeamMember teamMember = fixtureMonkey.giveMeOne(TeamMember.class);

			given(userRepo.findById(user.getId())).willReturn(Optional.of(user));
			given(teamMemberRepo.findByUserId(user.getId())).willReturn(Optional.of(teamMember));
			given(authRepo.existsByUserId(user.getId())).willReturn(true);

			//when
			UserResponse result = userService.findUser(user.getId());

			//then
			assertThat(result.isLocalAccount()).isFalse();
			assertThat(result.getId()).isEqualTo(user.getId());
			assertThat(result.getName()).isEqualTo(user.getUsername());
			assertThat(result.getTeamId()).isEqualTo(teamMember.getTeamId());
			assertThat(result.getTeamRole()).isEqualTo(teamMember.getRole());
			verify(userRepo).findById(user.getId());
			verify(teamMemberRepo).findByUserId(user.getId());
			verify(authRepo).existsByUserId(user.getId());
		}

		@Test
		@DisplayName("실패 - 유저 없음")
		public void fail_userNotFound() {
			//given
			Long userId = 99L;
			given(userRepo.findById(userId)).willReturn(Optional.empty());

			//when
			//then
			assertThatThrownBy(() -> userService.findUser(userId))
					.isInstanceOf(BusinessException.class)
					.hasMessageContaining("유저 정보를 찾을 수 없습니다.");

			verify(authRepo, never()).existsByUserId(anyLong());
		}
	}

	@Nested
	@DisplayName("유저 정보 업데이트")
	class UpdateUser {

		@Test
		@DisplayName("성공")
		public void success() {
			//given
			User user = fixtureMonkey.giveMeOne(User.class);
			User spyUser = spy(user);
			given(userRepo.findById(user.getId())).willReturn(Optional.of(spyUser));
			UserUpdateRequest req = fixtureMonkey.giveMeOne(UserUpdateRequest.class);

			//when
			userService.updateUser(user.getId(), req);

			//then
			verify(userRepo).findById(user.getId());
			verify(spyUser).update(req);
			verifyNoMoreInteractions(userRepo);
		}

		@Test
		@DisplayName("실패 - 유저 없음")
		public void fail_userNotFound() {
			//given
			Long userId = 99L;
			given(userRepo.findById(userId)).willReturn(Optional.empty());
			UserUpdateRequest req = fixtureMonkey.giveMeOne(UserUpdateRequest.class);

			//when
			//then
			assertThatThrownBy(() -> userService.updateUser(userId, req))
					.isInstanceOf(BusinessException.class)
					.hasMessageContaining("유저 정보를 찾을 수 없습니다.");
			verify(userRepo).findById(userId);
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
			given(userRepo.existsByNickname(trimmedNickname)).willReturn(false);

			//when
			boolean result = userService.existsByNickname(nickname);

			//then
			assertThat(result).isTrue();
			verify(userRepo).existsByNickname(trimmedNickname);
			verifyNoMoreInteractions(userRepo);
		}

		@Test
		@DisplayName("실패 - 이미 존재하는 닉네임")
		public void fail_existsNickname() {
			//given
			String nickname = "existingNickname";
			given(userRepo.existsByNickname(nickname)).willReturn(true);

			//when
			//then
			assertThatThrownBy(() -> userService.existsByNickname(nickname))
					.isInstanceOf(BusinessException.class)
					.hasMessageContaining("이미 사용중인 닉네임입니다.");

			verify(userRepo).existsByNickname(argThat(s -> s.equals(s.trim())));
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
			User user = fixtureMonkey.giveMeOne(User.class);
			User spyUser = spy(user);
			PasswordUpdateRequest req = fixtureMonkey.giveMeBuilder(PasswordUpdateRequest.class)
					.set("newPassword", "validPassword8chars")
					.sample();

			given(userRepo.findById(user.getId())).willReturn(Optional.of(spyUser));
			given(encoder.matches(req.getCurrentPassword(), spyUser.getPassword())).willReturn(true);
			given(encoder.encode(req.getNewPassword())).willReturn("encodedNewPassword");

			//when
			userService.updatePassword(user.getId(), req);

			//then
			verify(userRepo).findById(user.getId());
			verify(encoder).matches(req.getCurrentPassword(), user.getPassword());
			verify(encoder).encode(req.getNewPassword());
			verify(spyUser).updatePassword("encodedNewPassword");
			verifyNoMoreInteractions(userRepo, encoder);
		}

		@Test
		@DisplayName("실패 - 비밀번호 8글자 미만")
		public void fail_shortPassword() {
			//given
			Long userId = 1L;
			PasswordUpdateRequest req = fixtureMonkey.giveMeBuilder(PasswordUpdateRequest.class)
					.set("newPassword", "short")
					.sample();

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
			User user = fixtureMonkey.giveMeOne(User.class);
			PasswordUpdateRequest req = fixtureMonkey.giveMeBuilder(PasswordUpdateRequest.class)
					.set("newPassword", "newValidPassword8")
					.sample();

			given(userRepo.findById(user.getId())).willReturn(Optional.of(user));
			given(encoder.matches(req.getCurrentPassword(), user.getPassword())).willReturn(false);

			//when
			//then
			assertThatThrownBy(() -> userService.updatePassword(user.getId(), req))
					.isInstanceOf(BusinessException.class)
					.hasMessageContaining("현재 비밀번호가 일치하지 않습니다.");

			verify(userRepo).findById(user.getId());
			verify(encoder).matches(req.getCurrentPassword(), user.getPassword());
			verifyNoMoreInteractions(userRepo, encoder);
		}

		@Test
		@DisplayName("실패 - 유저 없음")
		public void fail_userNotFound() {
			//given
			Long userId = 99L;
			PasswordUpdateRequest req = fixtureMonkey.giveMeBuilder(PasswordUpdateRequest.class)
					.set("newPassword", "newValidPassword8")
					.sample();
			given(userRepo.findById(userId)).willReturn(Optional.empty());

			//when
			//then
			assertThatThrownBy(() -> userService.updatePassword(userId, req))
					.isInstanceOf(BusinessException.class)
					.hasMessageContaining("유저 정보를 찾을 수 없습니다.");

			verify(userRepo).findById(userId);
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
			User user = fixtureMonkey.giveMeOne(User.class);
			User spyUser = spy(user);
			given(userRepo.findById(user.getId())).willReturn(Optional.of(spyUser));

			//when
			userService.deleteUser(user.getId());

			//then
			verify(userRepo).findById(user.getId());
			verify(userRepo).delete(spyUser);
			verifyNoMoreInteractions(userRepo);
		}

		@Test
		@DisplayName("실패 - 유저 없음")
		public void fail_userNotFound() {
			//given
			Long userId = 99L;
			given(userRepo.findById(userId)).willReturn(Optional.empty());

			//when
			//then
			assertThatThrownBy(() -> userService.deleteUser(userId))
					.isInstanceOf(BusinessException.class)
					.hasMessageContaining("유저 정보를 찾을 수 없습니다.");

			verify(userRepo).findById(userId);
			verifyNoMoreInteractions(userRepo);
		}
	}
}
