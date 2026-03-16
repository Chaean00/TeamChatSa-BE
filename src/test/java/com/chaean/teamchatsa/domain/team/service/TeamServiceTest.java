package com.chaean.teamchatsa.domain.team.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import com.chaean.teamchatsa.domain.team.dto.request.TeamCreateRequest;
import com.chaean.teamchatsa.domain.team.dto.request.TeamJoinRequest;
import com.chaean.teamchatsa.domain.team.dto.response.TeamApplicationResponse;
import com.chaean.teamchatsa.domain.team.dto.response.TeamDetailResponse;
import com.chaean.teamchatsa.domain.team.dto.response.TeamListResponse;
import com.chaean.teamchatsa.domain.team.model.JoinStatus;
import com.chaean.teamchatsa.domain.team.model.Team;
import com.chaean.teamchatsa.domain.team.model.TeamApplication;
import com.chaean.teamchatsa.domain.team.model.TeamMember;
import com.chaean.teamchatsa.domain.team.model.TeamRole;
import com.chaean.teamchatsa.domain.team.repository.TeamJoinRequestRepository;
import com.chaean.teamchatsa.domain.team.repository.TeamMemberRepository;
import com.chaean.teamchatsa.domain.team.repository.TeamRepository;
import com.chaean.teamchatsa.domain.user.model.User;
import com.chaean.teamchatsa.domain.user.repository.UserRepository;
import com.chaean.teamchatsa.global.common.dto.SliceResponse;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

	private final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.build();
	@InjectMocks
	private TeamService teamService;
	@Mock
	private TeamRepository teamRepo;
	@Mock
	private TeamMemberRepository teamMemberRepo;
	@Mock
	private TeamJoinRequestRepository teamJoinRequestRepo;
	@Mock
	private UserRepository userRepo;
	@Mock
	private ApplicationEventPublisher eventPublisher;

	@Nested
	@DisplayName("팀 등록")
	class RegisterTeam {

		@Test
		@DisplayName("성공")
		void success() {
			// given
			Long userId = 1L;
			TeamCreateRequest req = fixtureMonkey.giveMeOne(TeamCreateRequest.class);
			given(teamMemberRepo.existsByUserId(userId)).willReturn(false);
			given(teamRepo.existsByName(req.getName())).willReturn(false);

			// when
			teamService.registerTeam(userId, req);

			// then
			verify(teamRepo).save(any(Team.class));
			verify(teamMemberRepo).save(any());
		}

		@Test
		@DisplayName("실패 - 이미 가입한 팀이 존재")
		void fail_already_joined() {
			// given
			Long userId = 1L;
			TeamCreateRequest req = fixtureMonkey.giveMeOne(TeamCreateRequest.class);
			given(teamMemberRepo.existsByUserId(userId)).willReturn(true);

			// when
			// then
			assertThatThrownBy(() -> teamService.registerTeam(userId, req))
					.isInstanceOf(BusinessException.class)
					.hasMessageContaining("이미 가입한 팀이 존재합니다.");
		}

		@Test
		@DisplayName("실패 - 중복된 팀명 존재")
		void fail_duplicate_team_name() {
			// given
			Long userId = 1L;
			TeamCreateRequest req = fixtureMonkey.giveMeOne(TeamCreateRequest.class);
			given(teamMemberRepo.existsByUserId(userId)).willReturn(false);
			given(teamRepo.existsByName(req.getName())).willReturn(true);

			// when
			// then
			assertThatThrownBy(() -> teamService.registerTeam(userId, req))
					.isInstanceOf(BusinessException.class)
					.hasMessageContaining("이미 존재하는 팀명입니다.");
		}
	}

	@Nested
	@DisplayName("팀 목록 조회")
	class FindTeamList {

		@Test
		@DisplayName("성공 - 팀명 필터 없음")
		void success_without_filter() {
			// given
			int page = 0;
			int size = 10;
			String teamName = null;
			Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
			Slice<TeamListResponse> sliceFromRepo = new SliceImpl<>(Collections.emptyList());
			// teamName이 null이면 findTeamListByNameWithPagination 호출
			given(teamRepo.findTeamListWithPagination(pageable)).willReturn(sliceFromRepo);

			// when
			SliceResponse<TeamListResponse> result = teamService.findTeamList(page, size, teamName);

			// then
			assertThat(result.getContent()).isEmpty();
			assertThat(result.isLast()).isTrue();
		}

		@Test
		@DisplayName("성공 - 팀명 필터 있음")
		void success_with_filter() {
			// given
			int page = 0;
			int size = 10;
			String teamName = "테스트팀";
			Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
			Slice<TeamListResponse> sliceFromRepo = new SliceImpl<>(Collections.emptyList());
			// teamName이 있으면 findTeamListWithPagination 호출
			given(teamRepo.findTeamListByNameWithPagination(pageable, teamName)).willReturn(sliceFromRepo);

			// when
			SliceResponse<TeamListResponse> result = teamService.findTeamList(page, size, teamName);

			// then
			assertThat(result.getContent()).isEmpty();
			assertThat(result.isLast()).isTrue();
		}
	}

	@Nested
	@DisplayName("팀 상세 조회")
	class FindTeamDetail {

		@Test
		@DisplayName("성공 - 팀원인 경우")
		void success_as_member() {
			// given
			Team team = fixtureMonkey.giveMeOne(Team.class);
			TeamMember teamMember = fixtureMonkey.giveMeBuilder(TeamMember.class)
					.set("teamId", team.getId())
					.set("role", TeamRole.LEADER)
					.sample();

			given(teamRepo.findById(team.getId())).willReturn(Optional.of(team));
			given(teamMemberRepo.countByTeamId(team.getId())).willReturn(1L);
			given(teamMemberRepo.findByTeamIdAndUserId(team.getId(), teamMember.getUserId()))
					.willReturn(Optional.of(teamMember));

			// when
			TeamDetailResponse result = teamService.findTeamDetail(team.getId(), teamMember.getUserId());

			// then
			assertThat(result.getName()).isEqualTo(team.getName());
			assertThat(result.getUserRole()).isEqualTo(TeamRole.LEADER);
		}

		@Test
		@DisplayName("성공 - 팀원이 아닌 경우")
		void success_not_member() {
			// given
			Team team = fixtureMonkey.giveMeOne(Team.class);
			Long userId = 2L;

			given(teamRepo.findById(team.getId())).willReturn(Optional.of(team));
			given(teamMemberRepo.countByTeamId(team.getId())).willReturn(1L);
			given(teamMemberRepo.findByTeamIdAndUserId(team.getId(), userId))
					.willReturn(Optional.empty());

			// when
			TeamDetailResponse result = teamService.findTeamDetail(team.getId(), userId);

			// then
			assertThat(result.getName()).isEqualTo(team.getName());
			assertThat(result.getUserRole()).isNull();
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 팀")
		void fail_not_found() {
			// given
			Long teamId = 1L;
			Long userId = 1L;
			given(teamRepo.findById(teamId)).willReturn(Optional.empty());

			// when
			// then
			assertThatThrownBy(() -> teamService.findTeamDetail(teamId, userId))
					.isInstanceOf(BusinessException.class)
					.hasMessageContaining("존재하지 않는 팀입니다.");
		}
	}

	@Nested
	@DisplayName("팀 가입 신청")
	class ApplyToTeam {

		@Test
		@DisplayName("성공")
		void success() {
			// given
			Long teamId = 1L;
			Long userId = 1L;
			TeamJoinRequest req = fixtureMonkey.giveMeBuilder(TeamJoinRequest.class)
					.set("message", "가입 희망합니다.")
					.sample();
			User applicant = fixtureMonkey.giveMeBuilder(User.class)
					.set("id", userId)
					.set("nickname", "applicant")
					.sample();
			TeamApplication savedApplication = fixtureMonkey.giveMeBuilder(TeamApplication.class)
					.set("id", 10L)
					.set("teamId", teamId)
					.set("userId", userId)
					.set("status", JoinStatus.PENDING)
					.sample();

			given(teamMemberRepo.existsByUserId(userId)).willReturn(false);
			given(teamJoinRequestRepo.existsByTeamIdAndUserId(teamId, userId)).willReturn(false);
			given(teamRepo.existsById(teamId)).willReturn(true);
			given(userRepo.findById(userId)).willReturn(Optional.of(applicant));
			given(teamJoinRequestRepo.save(any(TeamApplication.class))).willReturn(savedApplication);

			// when
			teamService.applyToTeam(teamId, userId, req);

			// then
			verify(teamJoinRequestRepo).save(any());
		}

		@Test
		@DisplayName("실패 - 이미 가입 신청한 팀")
		void fail_already_applied() {
			// given
			Long teamId = 1L;
			Long userId = 1L;
			TeamJoinRequest req = fixtureMonkey.giveMeOne(TeamJoinRequest.class);
			given(teamJoinRequestRepo.existsByTeamIdAndUserId(teamId, userId)).willReturn(true);

			// when
			// then
			assertThrows(BusinessException.class, () -> teamService.applyToTeam(teamId, userId, req));
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 팀")
		void fail_not_found() {
			// given
			Long teamId = 1L;
			Long userId = 1L;
			TeamJoinRequest req = fixtureMonkey.giveMeOne(TeamJoinRequest.class);
			given(teamJoinRequestRepo.existsByTeamIdAndUserId(teamId, userId)).willReturn(false);
			given(teamRepo.existsById(teamId)).willReturn(false);

			// when
			// then
			assertThatThrownBy(() -> teamService.applyToTeam(teamId, userId, req))
					.isInstanceOf(BusinessException.class)
					.hasMessageContaining("존재하지 않는 팀입니다.");
		}
	}

	@Nested
	@DisplayName("팀 삭제")
	class DeleteTeam {

		@Test
		@DisplayName("성공 - 리더 본인만 있는 팀 삭제")
		void success() {
			// given
			Team team = fixtureMonkey.giveMeBuilder(Team.class)
					.set("id", 1L)
					.set("leaderUserId", 10L)
					.set("name", "삭제할 팀")
					.sample();
			TeamMember leaderMember = fixtureMonkey.giveMeBuilder(TeamMember.class)
					.set("id", 1L)
					.set("teamId", team.getId())
					.set("userId", team.getLeaderUserId())
					.set("role", TeamRole.LEADER)
					.sample();

			given(teamRepo.findById(team.getId())).willReturn(Optional.of(team));
			given(teamMemberRepo.findByTeamId(team.getId())).willReturn(List.of(leaderMember));

			// when
			teamService.deleteTeam(team.getId());

			// then
			verify(teamRepo).delete(team);
			verify(teamMemberRepo).delete(leaderMember);
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 팀")
		void fail_team_not_found() {
			// given
			Long teamId = 1L;
			given(teamRepo.findById(teamId)).willReturn(Optional.empty());

			// when
			// then
			assertThatThrownBy(() -> teamService.deleteTeam(teamId))
					.isInstanceOf(BusinessException.class)
					.hasMessageContaining("존재하지 않는 팀입니다.");
		}

		@Test
		@DisplayName("실패 - 팀원이 2명 이상인 경우")
		void fail_has_members() {
			// given
			Team team = fixtureMonkey.giveMeBuilder(Team.class)
					.set("id", 1L)
					.set("leaderUserId", 10L)
					.set("name", "삭제 불가 팀")
					.sample();
			TeamMember leaderMember = fixtureMonkey.giveMeBuilder(TeamMember.class)
					.set("id", 1L)
					.set("teamId", team.getId())
					.set("userId", team.getLeaderUserId())
					.set("role", TeamRole.LEADER)
					.sample();
			TeamMember member = fixtureMonkey.giveMeBuilder(TeamMember.class)
					.set("id", 2L)
					.set("teamId", team.getId())
					.set("userId", 20L)
					.set("role", TeamRole.MEMBER)
					.sample();

			given(teamRepo.findById(team.getId())).willReturn(Optional.of(team));
			given(teamMemberRepo.findByTeamId(team.getId())).willReturn(List.of(leaderMember, member));

			// when
			// then
			assertThatThrownBy(() -> teamService.deleteTeam(team.getId()))
					.isInstanceOf(BusinessException.class)
					.hasMessageContaining("팀원 존재로 인해 팀을 삭제할 수 없습니다.");
		}

		@Test
		@DisplayName("실패 - 이미 삭제된 팀")
		void fail_already_deleted() {
			// given
			Long teamId = 1L;
			given(teamRepo.findById(teamId)).willReturn(Optional.empty());

			// when
			// then
			assertThatThrownBy(() -> teamService.deleteTeam(teamId))
					.isInstanceOf(BusinessException.class)
					.hasMessageContaining("존재하지 않는 팀입니다.");
		}
	}

	@Nested
	@DisplayName("팀 가입 신청 목록 조회")
	class FindTeamApplications {

		@Test
		@DisplayName("성공 - 팀장이 조회")
		void success_as_leader() {
			// given
			Long teamId = 1L;
			Long userId = 1L;
			TeamMember teamMember = fixtureMonkey.giveMeBuilder(TeamMember.class)
					.set("teamId", teamId)
					.set("userId", userId)
					.set("role", TeamRole.LEADER)
					.sample();
			List<TeamApplicationResponse> expected = Collections.emptyList();

			given(teamRepo.existsById(teamId)).willReturn(true);
			given(teamMemberRepo.findByTeamIdAndUserId(teamId, userId))
					.willReturn(Optional.of(teamMember));
			given(teamJoinRequestRepo.findApplicationsByTeamIdAndStatus(teamId, JoinStatus.PENDING))
					.willReturn(expected);

			// when
			List<TeamApplicationResponse> result = teamService.findTeamApplications(teamId, userId);

			// then
			assertThat(result).isEqualTo(expected);
			verify(teamRepo).existsById(teamId);
			verify(teamMemberRepo).findByTeamIdAndUserId(teamId, userId);
			verify(teamJoinRequestRepo).findApplicationsByTeamIdAndStatus(teamId, JoinStatus.PENDING);
		}

		@Test
		@DisplayName("성공 - 부팀장이 조회")
		void success_as_co_leader() {
			// given
			Long teamId = 1L;
			Long userId = 2L;
			TeamMember teamMember = fixtureMonkey.giveMeBuilder(TeamMember.class)
					.set("teamId", teamId)
					.set("userId", userId)
					.set("role", TeamRole.CO_LEADER)
					.sample();
			List<TeamApplicationResponse> expected = Collections.emptyList();

			given(teamRepo.existsById(teamId)).willReturn(true);
			given(teamMemberRepo.findByTeamIdAndUserId(teamId, userId))
					.willReturn(Optional.of(teamMember));
			given(teamJoinRequestRepo.findApplicationsByTeamIdAndStatus(teamId, JoinStatus.PENDING))
					.willReturn(expected);

			// when
			List<TeamApplicationResponse> result = teamService.findTeamApplications(teamId, userId);

			// then
			assertThat(result).isEqualTo(expected);
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 팀")
		void fail_team_not_found() {
			// given
			Long teamId = 1L;
			Long userId = 1L;
			given(teamRepo.existsById(teamId)).willReturn(false);

			// when
			// then
			assertThatThrownBy(() -> teamService.findTeamApplications(teamId, userId))
					.isInstanceOf(BusinessException.class)
					.hasMessageContaining("존재하지 않는 팀입니다.");
		}

		@Test
		@DisplayName("실패 - 팀 멤버가 아님")
		void fail_not_team_member() {
			// given
			Long teamId = 1L;
			Long userId = 1L;
			given(teamRepo.existsById(teamId)).willReturn(true);
			given(teamMemberRepo.findByTeamIdAndUserId(teamId, userId))
					.willReturn(Optional.empty());

			// when
			// then
			assertThatThrownBy(() -> teamService.findTeamApplications(teamId, userId))
					.isInstanceOf(BusinessException.class)
					.hasMessageContaining("해당 팀의 멤버가 아닙니다.");
		}

		@Test
		@DisplayName("실패 - 일반 멤버 권한 없음")
		void fail_no_permission() {
			// given
			Long teamId = 1L;
			Long userId = 3L;
			TeamMember teamMember = fixtureMonkey.giveMeBuilder(TeamMember.class)
					.set("teamId", teamId)
					.set("userId", userId)
					.set("role", TeamRole.MEMBER)
					.sample();

			given(teamRepo.existsById(teamId)).willReturn(true);
			given(teamMemberRepo.findByTeamIdAndUserId(teamId, userId))
					.willReturn(Optional.of(teamMember));

			// when
			// then
			assertThatThrownBy(() -> teamService.findTeamApplications(teamId, userId))
					.isInstanceOf(BusinessException.class)
					.hasMessageContaining("팀 가입 신청 목록을 조회할 권한이 없습니다.");
		}
	}

	@Nested
	@DisplayName("팀 가입 신청 수락")
	class AcceptTeamApplication {

		@Test
		@DisplayName("성공")
		void success() {
			// given
			Long teamId = 1L;
			Long userId = 1L;
			Long applicantUserId = 2L;
			Team team = fixtureMonkey.giveMeBuilder(Team.class)
					.set("id", teamId)
					.set("leaderUserId", userId)
					.set("name", "테스트팀")
					.sample();
			TeamMember teamMember = fixtureMonkey.giveMeBuilder(TeamMember.class)
					.set("id", 1L)
					.set("teamId", teamId)
					.set("userId", userId)
					.set("role", TeamRole.LEADER)
					.sample();
			TeamApplication application = fixtureMonkey.giveMeBuilder(TeamApplication.class)
					.set("id", 100L)
					.set("teamId", teamId)
					.set("userId", applicantUserId)
					.set("status", JoinStatus.PENDING)
					.sample();

			given(teamRepo.findById(teamId)).willReturn(Optional.of(team));
			given(teamMemberRepo.findByTeamIdAndUserId(teamId, userId))
					.willReturn(Optional.of(teamMember));
			given(teamJoinRequestRepo.findByIdAndTeamId(application.getId(), teamId))
					.willReturn(Optional.of(application));
			given(teamMemberRepo.existsByUserId(application.getUserId())).willReturn(false);
			given(teamJoinRequestRepo.findPendingApplicationsByUserIdExcluding(application.getUserId(), application.getId()))
					.willReturn(List.of());

			// when
			teamService.acceptTeamApplication(teamId, application.getId(), userId);

			// then
			assertThat(application.getStatus()).isEqualTo(JoinStatus.ACCEPTED);
			verify(teamMemberRepo).save(any(TeamMember.class));
			verify(teamJoinRequestRepo).findPendingApplicationsByUserIdExcluding(application.getUserId(), application.getId());
		}

		@Test
		@DisplayName("성공 - 다른 팀의 PENDING 신청 자동 거절")
		void success_with_auto_reject_other_applications() {
			// given
			Long teamId = 1L;
			Long userId = 1L;
			Long applicantUserId = 2L;
			Team team = fixtureMonkey.giveMeBuilder(Team.class)
					.set("id", teamId)
					.set("leaderUserId", userId)
					.set("name", "테스트팀")
					.sample();
			TeamMember teamMember = fixtureMonkey.giveMeBuilder(TeamMember.class)
					.set("id", 1L)
					.set("teamId", teamId)
					.set("userId", userId)
					.set("role", TeamRole.LEADER)
					.sample();
			TeamApplication acceptedApplication = fixtureMonkey.giveMeBuilder(TeamApplication.class)
					.set("id", 100L)
					.set("teamId", teamId)
					.set("userId", applicantUserId)
					.set("status", JoinStatus.PENDING)
					.sample();
			TeamApplication otherApplication1 = fixtureMonkey.giveMeBuilder(TeamApplication.class)
					.set("id", 101L)
					.set("teamId", 2L)
					.set("userId", applicantUserId)
					.set("status", JoinStatus.PENDING)
					.sample();
			TeamApplication otherApplication2 = fixtureMonkey.giveMeBuilder(TeamApplication.class)
					.set("id", 102L)
					.set("teamId", 3L)
					.set("userId", applicantUserId)
					.set("status", JoinStatus.PENDING)
					.sample();

			given(teamRepo.findById(teamId)).willReturn(Optional.of(team));
			given(teamMemberRepo.findByTeamIdAndUserId(teamId, userId))
					.willReturn(Optional.of(teamMember));
			given(teamJoinRequestRepo.findByIdAndTeamId(acceptedApplication.getId(), teamId))
					.willReturn(Optional.of(acceptedApplication));
			given(teamMemberRepo.existsByUserId(acceptedApplication.getUserId())).willReturn(false);
			given(teamJoinRequestRepo.findPendingApplicationsByUserIdExcluding(acceptedApplication.getUserId(),
					acceptedApplication.getId()))
					.willReturn(List.of(otherApplication1, otherApplication2));

			// when
			teamService.acceptTeamApplication(teamId, acceptedApplication.getId(), userId);

			// then
			assertThat(acceptedApplication.getStatus()).isEqualTo(JoinStatus.ACCEPTED);
			assertThat(otherApplication1.getStatus()).isEqualTo(JoinStatus.REJECTED);
			assertThat(otherApplication2.getStatus()).isEqualTo(JoinStatus.REJECTED);
			verify(teamMemberRepo).save(any(TeamMember.class));
			verify(teamJoinRequestRepo).findPendingApplicationsByUserIdExcluding(acceptedApplication.getUserId(),
					acceptedApplication.getId());
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 팀")
		void fail_team_not_found() {
			// given
			Long teamId = 1L;
			Long applicationId = 100L;
			Long userId = 1L;
			lenient().when(teamRepo.findById(teamId)).thenReturn(Optional.empty());

			// when
			// then
			assertThatThrownBy(() -> teamService.acceptTeamApplication(teamId, applicationId, userId))
					.isInstanceOf(BusinessException.class)
					.hasMessageContaining("존재하지 않는 팀입니다.");
		}

		@Test
		@DisplayName("실패 - 권한 없음")
		void fail_no_permission() {
			// given
			Long teamId = 1L;
			Long applicationId = 100L;
			Long userId = 3L;
			Team team = fixtureMonkey.giveMeBuilder(Team.class)
					.set("id", teamId)
					.set("leaderUserId", 1L)
					.set("name", "테스트팀")
					.sample();
			TeamMember teamMember = fixtureMonkey.giveMeBuilder(TeamMember.class)
					.set("id", 1L)
					.set("teamId", teamId)
					.set("userId", userId)
					.set("role", TeamRole.MEMBER)
					.sample();

			given(teamRepo.findById(teamId)).willReturn(Optional.of(team));
			given(teamMemberRepo.findByTeamIdAndUserId(teamId, userId))
					.willReturn(Optional.of(teamMember));

			// when
			// then
			assertThatThrownBy(() -> teamService.acceptTeamApplication(teamId, applicationId, userId))
					.isInstanceOf(BusinessException.class)
					.hasMessageContaining("팀 가입 신청을 수락할 권한이 없습니다.");
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 가입 신청")
		void fail_application_not_found() {
			// given
			Long teamId = 1L;
			Long applicationId = 100L;
			Long userId = 1L;
			Team team = fixtureMonkey.giveMeBuilder(Team.class)
					.set("id", teamId)
					.set("leaderUserId", userId)
					.set("name", "테스트팀")
					.sample();
			TeamMember teamMember = fixtureMonkey.giveMeBuilder(TeamMember.class)
					.set("id", 1L)
					.set("teamId", teamId)
					.set("userId", userId)
					.set("role", TeamRole.LEADER)
					.sample();

			given(teamRepo.findById(teamId)).willReturn(Optional.of(team));
			given(teamMemberRepo.findByTeamIdAndUserId(teamId, userId))
					.willReturn(Optional.of(teamMember));
			given(teamJoinRequestRepo.findByIdAndTeamId(applicationId, teamId))
					.willReturn(Optional.empty());

			// when
			// then
			assertThatThrownBy(() -> teamService.acceptTeamApplication(teamId, applicationId, userId))
					.isInstanceOf(BusinessException.class)
					.hasMessageContaining("존재하지 않는 가입 신청입니다.");
		}

		@Test
		@DisplayName("실패 - 이미 처리된 신청")
		void fail_already_processed() {
			// given
			Long teamId = 1L;
			Long userId = 1L;
			Team team = fixtureMonkey.giveMeBuilder(Team.class)
					.set("id", teamId)
					.set("leaderUserId", userId)
					.set("name", "테스트팀")
					.sample();
			TeamMember teamMember = fixtureMonkey.giveMeBuilder(TeamMember.class)
					.set("id", 1L)
					.set("teamId", teamId)
					.set("userId", userId)
					.set("role", TeamRole.LEADER)
					.sample();
			TeamApplication application = fixtureMonkey.giveMeBuilder(TeamApplication.class)
					.set("id", 100L)
					.set("teamId", teamId)
					.set("userId", 2L)
					.set("status", JoinStatus.ACCEPTED)
					.sample();

			given(teamRepo.findById(teamId)).willReturn(Optional.of(team));
			given(teamMemberRepo.findByTeamIdAndUserId(teamId, userId))
					.willReturn(Optional.of(teamMember));
			given(teamJoinRequestRepo.findByIdAndTeamId(application.getId(), teamId))
					.willReturn(Optional.of(application));

			// when
			// then
			assertThatThrownBy(() -> teamService.acceptTeamApplication(teamId, application.getId(), userId))
					.isInstanceOf(BusinessException.class)
					.hasMessageContaining("이미 처리된 가입 신청입니다.");
		}

		@Test
		@DisplayName("실패 - 신청자가 이미 다른 팀에 가입됨")
		void fail_already_member() {
			// given
			Long teamId = 1L;
			Long userId = 1L;
			Team team = fixtureMonkey.giveMeBuilder(Team.class)
					.set("id", teamId)
					.set("leaderUserId", userId)
					.set("name", "테스트팀")
					.sample();
			TeamMember teamMember = fixtureMonkey.giveMeBuilder(TeamMember.class)
					.set("id", 1L)
					.set("teamId", teamId)
					.set("userId", userId)
					.set("role", TeamRole.LEADER)
					.sample();
			TeamApplication application = fixtureMonkey.giveMeBuilder(TeamApplication.class)
					.set("id", 100L)
					.set("teamId", teamId)
					.set("userId", 2L)
					.set("status", JoinStatus.PENDING)
					.sample();

			given(teamRepo.findById(teamId)).willReturn(Optional.of(team));
			given(teamMemberRepo.findByTeamIdAndUserId(teamId, userId))
					.willReturn(Optional.of(teamMember));
			given(teamJoinRequestRepo.findByIdAndTeamId(application.getId(), teamId))
					.willReturn(Optional.of(application));
			given(teamMemberRepo.existsByUserId(application.getUserId())).willReturn(true);

			// when
			// then
			assertThatThrownBy(() -> teamService.acceptTeamApplication(teamId, application.getId(), userId))
					.isInstanceOf(BusinessException.class)
					.hasMessageContaining("이미 다른 팀에 가입된 사용자입니다.");
		}
	}

	@Nested
	@DisplayName("팀 가입 신청 거절")
	class RejectTeamApplication {

		@Test
		@DisplayName("성공")
		void success() {
			// given
			Long teamId = 1L;
			Long userId = 1L;
			Team team = fixtureMonkey.giveMeBuilder(Team.class)
					.set("id", teamId)
					.set("leaderUserId", userId)
					.set("name", "테스트팀")
					.sample();
			TeamMember teamMember = fixtureMonkey.giveMeBuilder(TeamMember.class)
					.set("id", 1L)
					.set("teamId", teamId)
					.set("userId", userId)
					.set("role", TeamRole.LEADER)
					.sample();
			TeamApplication application = fixtureMonkey.giveMeBuilder(TeamApplication.class)
					.set("id", 100L)
					.set("teamId", teamId)
					.set("userId", 2L)
					.set("status", JoinStatus.PENDING)
					.sample();

			given(teamRepo.findById(teamId)).willReturn(Optional.of(team));
			given(teamMemberRepo.findByTeamIdAndUserId(teamId, userId))
					.willReturn(Optional.of(teamMember));
			given(teamJoinRequestRepo.findByIdAndTeamId(application.getId(), teamId))
					.willReturn(Optional.of(application));

			// when
			teamService.rejectTeamApplication(teamId, application.getId(), userId);

			// then
			assertThat(application.getStatus()).isEqualTo(JoinStatus.REJECTED);
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 팀")
		void fail_team_not_found() {
			// given
			Long teamId = 1L;
			Long applicationId = 100L;
			Long userId = 1L;
			lenient().when(teamRepo.findById(teamId)).thenReturn(Optional.empty());

			// when
			// then
			assertThatThrownBy(() -> teamService.rejectTeamApplication(teamId, applicationId, userId))
					.isInstanceOf(BusinessException.class)
					.hasMessageContaining("존재하지 않는 팀입니다.");
		}

		@Test
		@DisplayName("실패 - 권한 없음")
		void fail_no_permission() {
			// given
			Long teamId = 1L;
			Long applicationId = 100L;
			Long userId = 3L;
			Team team = fixtureMonkey.giveMeBuilder(Team.class)
					.set("id", teamId)
					.set("leaderUserId", 1L)
					.set("name", "테스트팀")
					.sample();
			TeamMember teamMember = fixtureMonkey.giveMeBuilder(TeamMember.class)
					.set("id", 1L)
					.set("teamId", teamId)
					.set("userId", userId)
					.set("role", TeamRole.MEMBER)
					.sample();

			given(teamRepo.findById(teamId)).willReturn(Optional.of(team));
			given(teamMemberRepo.findByTeamIdAndUserId(teamId, userId))
					.willReturn(Optional.of(teamMember));

			// when
			// then
			assertThatThrownBy(() -> teamService.rejectTeamApplication(teamId, applicationId, userId))
					.isInstanceOf(BusinessException.class)
					.hasMessageContaining("팀 가입 신청을 거절할 권한이 없습니다.");
		}

		@Test
		@DisplayName("실패 - 이미 처리된 신청")
		void fail_already_processed() {
			// given
			Long teamId = 1L;
			Long userId = 1L;
			Team team = fixtureMonkey.giveMeBuilder(Team.class)
					.set("id", teamId)
					.set("leaderUserId", userId)
					.set("name", "테스트팀")
					.sample();
			TeamMember teamMember = fixtureMonkey.giveMeBuilder(TeamMember.class)
					.set("id", 1L)
					.set("teamId", teamId)
					.set("userId", userId)
					.set("role", TeamRole.LEADER)
					.sample();
			TeamApplication application = fixtureMonkey.giveMeBuilder(TeamApplication.class)
					.set("id", 100L)
					.set("teamId", teamId)
					.set("userId", 2L)
					.set("status", JoinStatus.REJECTED)
					.sample();

			given(teamRepo.findById(teamId)).willReturn(Optional.of(team));
			given(teamMemberRepo.findByTeamIdAndUserId(teamId, userId))
					.willReturn(Optional.of(teamMember));
			given(teamJoinRequestRepo.findByIdAndTeamId(application.getId(), teamId))
					.willReturn(Optional.of(application));

			// when
			// then
			assertThatThrownBy(() -> teamService.rejectTeamApplication(teamId, application.getId(), userId))
					.isInstanceOf(BusinessException.class)
					.hasMessageContaining("이미 처리된 가입 신청입니다.");
		}
	}

}
