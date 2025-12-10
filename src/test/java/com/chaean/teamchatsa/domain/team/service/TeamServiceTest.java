package com.chaean.teamchatsa.domain.team.service;

import com.chaean.teamchatsa.domain.team.dto.request.TeamCreateReq;
import com.chaean.teamchatsa.domain.team.dto.request.TeamJoinReq;
import com.chaean.teamchatsa.domain.team.dto.response.TeamApplicationRes;
import com.chaean.teamchatsa.domain.team.dto.response.TeamDetailRes;
import com.chaean.teamchatsa.domain.team.dto.response.TeamListRes;
import com.chaean.teamchatsa.domain.team.model.*;
import com.chaean.teamchatsa.domain.team.repository.TeamJoinRequestRepository;
import com.chaean.teamchatsa.domain.team.repository.TeamMemberRepository;
import com.chaean.teamchatsa.domain.team.repository.TeamRepository;
import com.chaean.teamchatsa.global.common.dto.SliceResponse;
import com.chaean.teamchatsa.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @InjectMocks
    private TeamService teamService;

    @Mock
    private TeamRepository teamRepo;

    @Mock
    private TeamMemberRepository teamMemberRepo;

    @Mock
    private TeamJoinRequestRepository teamJoinRequestRepo;

    @Nested
    @DisplayName("팀 등록")
    class RegisterTeam {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            Long userId = 1L;
            TeamCreateReq req = new TeamCreateReq("teamName", "area", "description", ContactType.KAKAO, "contact", "imgUrl", "하하");
            given(teamMemberRepo.existsByUserIdAndIsDeletedFalse(userId)).willReturn(false);
            given(teamRepo.existsByNameAndIsDeletedFalse(req.getName())).willReturn(false);

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
            TeamCreateReq req = new TeamCreateReq("teamName", "area", "description", ContactType.KAKAO, "contact", "imgUrl", "중하");
            given(teamMemberRepo.existsByUserIdAndIsDeletedFalse(userId)).willReturn(true);

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
            TeamCreateReq req = new TeamCreateReq("teamName", "area", "description", ContactType.KAKAO, "contact", "imgUrl", "중상");
            given(teamMemberRepo.existsByUserIdAndIsDeletedFalse(userId)).willReturn(false);
            given(teamRepo.existsByNameAndIsDeletedFalse(req.getName())).willReturn(true);

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
            Slice<TeamListRes> sliceFromRepo = new SliceImpl<>(Collections.emptyList());
            // teamName이 null이면 findTeamListByNameWithPagination 호출
            given(teamRepo.findTeamListWithPagination(pageable)).willReturn(sliceFromRepo);

            // when
            SliceResponse<TeamListRes> result = teamService.findTeamList(page, size, teamName);

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
            Slice<TeamListRes> sliceFromRepo = new SliceImpl<>(Collections.emptyList());
            // teamName이 있으면 findTeamListWithPagination 호출
            given(teamRepo.findTeamListByNameWithPagination(pageable, teamName)).willReturn(sliceFromRepo);

            // when
            SliceResponse<TeamListRes> result = teamService.findTeamList(page, size, teamName);

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
            Long teamId = 1L;
            Long userId = 1L;
            Team team = Team.builder()
                    .id(teamId)
                    .name("teamName")
                    .build();
            given(teamRepo.findByIdAndIsDeletedFalse(teamId)).willReturn(Optional.of(team));
            given(teamMemberRepo.countByTeamIdAndIsDeletedFalse(teamId)).willReturn(1L);
            given(teamMemberRepo.findByTeamIdAndUserIdAndIsDeletedFalse(teamId, userId))
                    .willReturn(Optional.of(com.chaean.teamchatsa.domain.team.model.TeamMember.builder()
                            .teamId(teamId)
                            .userId(userId)
                            .role(com.chaean.teamchatsa.domain.team.model.TeamRole.LEADER)
                            .build()));

            // when
            TeamDetailRes result = teamService.findTeamDetail(teamId, userId);

            // then
            assertThat(result.getName()).isEqualTo("teamName");
            assertThat(result.getUserRole()).isEqualTo(com.chaean.teamchatsa.domain.team.model.TeamRole.LEADER);
        }

        @Test
        @DisplayName("성공 - 팀원이 아닌 경우")
        void success_not_member() {
            // given
            Long teamId = 1L;
            Long userId = 2L;
            Team team = Team.builder()
                    .id(teamId)
                    .name("teamName")
                    .build();
            given(teamRepo.findByIdAndIsDeletedFalse(teamId)).willReturn(Optional.of(team));
            given(teamMemberRepo.countByTeamIdAndIsDeletedFalse(teamId)).willReturn(1L);
            given(teamMemberRepo.findByTeamIdAndUserIdAndIsDeletedFalse(teamId, userId))
                    .willReturn(Optional.empty());

            // when
            TeamDetailRes result = teamService.findTeamDetail(teamId, userId);

            // then
            assertThat(result.getName()).isEqualTo("teamName");
            assertThat(result.getUserRole()).isNull();
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 팀")
        void fail_not_found() {
            // given
            Long teamId = 1L;
            Long userId = 1L;
            given(teamRepo.findByIdAndIsDeletedFalse(teamId)).willReturn(Optional.empty());

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
            TeamJoinReq req = new TeamJoinReq("message");

            given(teamJoinRequestRepo.existsByTeamIdAndUserId(teamId, userId)).willReturn(false);
            given(teamRepo.existsByIdAndIsDeletedFalse(teamId)).willReturn(true);

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
            TeamJoinReq req = new TeamJoinReq("message");
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
            TeamJoinReq req = new TeamJoinReq("message");
            given(teamJoinRequestRepo.existsByTeamIdAndUserId(teamId, userId)).willReturn(false);
            given(teamRepo.existsByIdAndIsDeletedFalse(teamId)).willReturn(false);

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
            Long teamId = 1L;
            Team team = Team.builder()
                    .id(teamId)
                    .leaderUserId(1L)
                    .name("teamName")
                    .area("서울")
                    .description("설명")
                    .contactType(ContactType.KAKAO)
                    .contact("contact")
                    .build();

            given(teamRepo.findByIdAndIsDeletedFalse(teamId)).willReturn(Optional.of(team));
            given(teamMemberRepo.countByTeamIdAndIsDeletedFalse(teamId)).willReturn(1L);

            // when
            teamService.deleteTeam(teamId);

            // then
            assertThat(team.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 팀")
        void fail_team_not_found() {
            // given
            Long teamId = 1L;
            given(teamRepo.findByIdAndIsDeletedFalse(teamId)).willReturn(Optional.empty());

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
            Long teamId = 1L;
            Team team = Team.builder()
                    .id(teamId)
                    .leaderUserId(1L)
                    .name("teamName")
                    .area("서울")
                    .description("설명")
                    .contactType(ContactType.KAKAO)
                    .contact("contact")
                    .build();

            given(teamRepo.findByIdAndIsDeletedFalse(teamId)).willReturn(Optional.of(team));
            given(teamMemberRepo.countByTeamIdAndIsDeletedFalse(teamId)).willReturn(3L); // 리더 포함 3명

            // when
            // then
            assertThatThrownBy(() -> teamService.deleteTeam(teamId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("팀원 존재로 인해 팀을 삭제할 수 없습니다.");
        }

        @Test
        @DisplayName("실패 - 이미 삭제된 팀")
        void fail_already_deleted() {
            // given
            Long teamId = 1L;
            given(teamRepo.findByIdAndIsDeletedFalse(teamId)).willReturn(Optional.empty());

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
            TeamMember teamMember = TeamMember.builder()
                    .teamId(teamId)
                    .userId(userId)
                    .role(TeamRole.LEADER)
                    .build();
            List<TeamApplicationRes> expected = Collections.emptyList();

            given(teamRepo.existsByIdAndIsDeletedFalse(teamId)).willReturn(true);
            given(teamMemberRepo.findByTeamIdAndUserIdAndIsDeletedFalse(teamId, userId))
                    .willReturn(Optional.of(teamMember));
            given(teamJoinRequestRepo.findApplicationsByTeamIdAndStatus(teamId, JoinStatus.PENDING))
                    .willReturn(expected);

            // when
            List<TeamApplicationRes> result = teamService.findTeamApplications(teamId, userId);

            // then
            assertThat(result).isEqualTo(expected);
            verify(teamRepo).existsByIdAndIsDeletedFalse(teamId);
            verify(teamMemberRepo).findByTeamIdAndUserIdAndIsDeletedFalse(teamId, userId);
            verify(teamJoinRequestRepo).findApplicationsByTeamIdAndStatus(teamId, JoinStatus.PENDING);
        }

        @Test
        @DisplayName("성공 - 부팀장이 조회")
        void success_as_co_leader() {
            // given
            Long teamId = 1L;
            Long userId = 2L;
            TeamMember teamMember = TeamMember.builder()
                    .teamId(teamId)
                    .userId(userId)
                    .role(TeamRole.CO_LEADER)
                    .build();
            List<TeamApplicationRes> expected = Collections.emptyList();

            given(teamRepo.existsByIdAndIsDeletedFalse(teamId)).willReturn(true);
            given(teamMemberRepo.findByTeamIdAndUserIdAndIsDeletedFalse(teamId, userId))
                    .willReturn(Optional.of(teamMember));
            given(teamJoinRequestRepo.findApplicationsByTeamIdAndStatus(teamId, JoinStatus.PENDING))
                    .willReturn(expected);

            // when
            List<TeamApplicationRes> result = teamService.findTeamApplications(teamId, userId);

            // then
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 팀")
        void fail_team_not_found() {
            // given
            Long teamId = 1L;
            Long userId = 1L;
            given(teamRepo.existsByIdAndIsDeletedFalse(teamId)).willReturn(false);

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
            given(teamRepo.existsByIdAndIsDeletedFalse(teamId)).willReturn(true);
            given(teamMemberRepo.findByTeamIdAndUserIdAndIsDeletedFalse(teamId, userId))
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
            TeamMember teamMember = TeamMember.builder()
                    .teamId(teamId)
                    .userId(userId)
                    .role(TeamRole.MEMBER)
                    .build();

            given(teamRepo.existsByIdAndIsDeletedFalse(teamId)).willReturn(true);
            given(teamMemberRepo.findByTeamIdAndUserIdAndIsDeletedFalse(teamId, userId))
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
            Long applicationId = 100L;
            Long userId = 1L;
            Long applicantUserId = 2L;

            TeamMember teamMember = TeamMember.builder()
                    .teamId(teamId)
                    .userId(userId)
                    .role(TeamRole.LEADER)
                    .build();

            TeamApplication application = TeamApplication.builder()
                    .id(applicationId)
                    .teamId(teamId)
                    .userId(applicantUserId)
                    .status(JoinStatus.PENDING)
                    .message("가입 신청합니다")
                    .build();

            given(teamRepo.existsByIdAndIsDeletedFalse(teamId)).willReturn(true);
            given(teamMemberRepo.findByTeamIdAndUserIdAndIsDeletedFalse(teamId, userId))
                    .willReturn(Optional.of(teamMember));
            given(teamJoinRequestRepo.findByIdAndTeamId(applicationId, teamId))
                    .willReturn(Optional.of(application));
            given(teamMemberRepo.existsByUserIdAndIsDeletedFalse(applicantUserId)).willReturn(false);
            given(teamJoinRequestRepo.findPendingApplicationsByUserIdExcluding(applicantUserId, applicationId))
                    .willReturn(List.of());

            // when
            teamService.acceptTeamApplication(teamId, applicationId, userId);

            // then
            assertThat(application.getStatus()).isEqualTo(JoinStatus.ACCEPTED);
            verify(teamMemberRepo).save(any(TeamMember.class));
            verify(teamJoinRequestRepo).findPendingApplicationsByUserIdExcluding(applicantUserId, applicationId);
        }

        @Test
        @DisplayName("성공 - 다른 팀의 PENDING 신청 자동 거절")
        void success_with_auto_reject_other_applications() {
            // given
            Long teamId = 1L;
            Long applicationId = 100L;
            Long userId = 1L;
            Long applicantUserId = 2L;

            TeamMember teamMember = TeamMember.builder()
                    .teamId(teamId)
                    .userId(userId)
                    .role(TeamRole.LEADER)
                    .build();

            TeamApplication acceptedApplication = TeamApplication.builder()
                    .id(applicationId)
                    .teamId(teamId)
                    .userId(applicantUserId)
                    .status(JoinStatus.PENDING)
                    .message("가입 신청합니다")
                    .build();

            // 다른 팀에 넣은 PENDING 신청들
            TeamApplication otherApplication1 = TeamApplication.builder()
                    .id(101L)
                    .teamId(2L)
                    .userId(applicantUserId)
                    .status(JoinStatus.PENDING)
                    .build();

            TeamApplication otherApplication2 = TeamApplication.builder()
                    .id(102L)
                    .teamId(3L)
                    .userId(applicantUserId)
                    .status(JoinStatus.PENDING)
                    .build();

            given(teamRepo.existsByIdAndIsDeletedFalse(teamId)).willReturn(true);
            given(teamMemberRepo.findByTeamIdAndUserIdAndIsDeletedFalse(teamId, userId))
                    .willReturn(Optional.of(teamMember));
            given(teamJoinRequestRepo.findByIdAndTeamId(applicationId, teamId))
                    .willReturn(Optional.of(acceptedApplication));
            given(teamMemberRepo.existsByUserIdAndIsDeletedFalse(applicantUserId)).willReturn(false);
            given(teamJoinRequestRepo.findPendingApplicationsByUserIdExcluding(applicantUserId, applicationId))
                    .willReturn(List.of(otherApplication1, otherApplication2));

            // when
            teamService.acceptTeamApplication(teamId, applicationId, userId);

            // then
            assertThat(acceptedApplication.getStatus()).isEqualTo(JoinStatus.ACCEPTED);
            assertThat(otherApplication1.getStatus()).isEqualTo(JoinStatus.REJECTED);
            assertThat(otherApplication2.getStatus()).isEqualTo(JoinStatus.REJECTED);
            verify(teamMemberRepo).save(any(TeamMember.class));
            verify(teamJoinRequestRepo).findPendingApplicationsByUserIdExcluding(applicantUserId, applicationId);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 팀")
        void fail_team_not_found() {
            // given
            Long teamId = 1L;
            Long applicationId = 100L;
            Long userId = 1L;
            given(teamRepo.existsByIdAndIsDeletedFalse(teamId)).willReturn(false);

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

            TeamMember teamMember = TeamMember.builder()
                    .teamId(teamId)
                    .userId(userId)
                    .role(TeamRole.MEMBER)
                    .build();

            given(teamRepo.existsByIdAndIsDeletedFalse(teamId)).willReturn(true);
            given(teamMemberRepo.findByTeamIdAndUserIdAndIsDeletedFalse(teamId, userId))
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

            TeamMember teamMember = TeamMember.builder()
                    .teamId(teamId)
                    .userId(userId)
                    .role(TeamRole.LEADER)
                    .build();

            given(teamRepo.existsByIdAndIsDeletedFalse(teamId)).willReturn(true);
            given(teamMemberRepo.findByTeamIdAndUserIdAndIsDeletedFalse(teamId, userId))
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
            Long applicationId = 100L;
            Long userId = 1L;

            TeamMember teamMember = TeamMember.builder()
                    .teamId(teamId)
                    .userId(userId)
                    .role(TeamRole.LEADER)
                    .build();

            TeamApplication application = TeamApplication.builder()
                    .id(applicationId)
                    .teamId(teamId)
                    .userId(2L)
                    .status(JoinStatus.ACCEPTED)
                    .build();

            given(teamRepo.existsByIdAndIsDeletedFalse(teamId)).willReturn(true);
            given(teamMemberRepo.findByTeamIdAndUserIdAndIsDeletedFalse(teamId, userId))
                    .willReturn(Optional.of(teamMember));
            given(teamJoinRequestRepo.findByIdAndTeamId(applicationId, teamId))
                    .willReturn(Optional.of(application));

            // when
            // then
            assertThatThrownBy(() -> teamService.acceptTeamApplication(teamId, applicationId, userId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이미 처리된 가입 신청입니다.");
        }

        @Test
        @DisplayName("실패 - 신청자가 이미 다른 팀에 가입됨")
        void fail_already_member() {
            // given
            Long teamId = 1L;
            Long applicationId = 100L;
            Long userId = 1L;
            Long applicantUserId = 2L;

            TeamMember teamMember = TeamMember.builder()
                    .teamId(teamId)
                    .userId(userId)
                    .role(TeamRole.LEADER)
                    .build();

            TeamApplication application = TeamApplication.builder()
                    .id(applicationId)
                    .teamId(teamId)
                    .userId(applicantUserId)
                    .status(JoinStatus.PENDING)
                    .build();

            given(teamRepo.existsByIdAndIsDeletedFalse(teamId)).willReturn(true);
            given(teamMemberRepo.findByTeamIdAndUserIdAndIsDeletedFalse(teamId, userId))
                    .willReturn(Optional.of(teamMember));
            given(teamJoinRequestRepo.findByIdAndTeamId(applicationId, teamId))
                    .willReturn(Optional.of(application));
            given(teamMemberRepo.existsByUserIdAndIsDeletedFalse(applicantUserId)).willReturn(true);

            // when
            // then
            assertThatThrownBy(() -> teamService.acceptTeamApplication(teamId, applicationId, userId))
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
            Long applicationId = 100L;
            Long userId = 1L;

            TeamMember teamMember = TeamMember.builder()
                    .teamId(teamId)
                    .userId(userId)
                    .role(TeamRole.LEADER)
                    .build();

            TeamApplication application = TeamApplication.builder()
                    .id(applicationId)
                    .teamId(teamId)
                    .userId(2L)
                    .status(JoinStatus.PENDING)
                    .message("가입 신청합니다")
                    .build();

            given(teamRepo.existsByIdAndIsDeletedFalse(teamId)).willReturn(true);
            given(teamMemberRepo.findByTeamIdAndUserIdAndIsDeletedFalse(teamId, userId))
                    .willReturn(Optional.of(teamMember));
            given(teamJoinRequestRepo.findByIdAndTeamId(applicationId, teamId))
                    .willReturn(Optional.of(application));

            // when
            teamService.rejectTeamApplication(teamId, applicationId, userId);

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
            given(teamRepo.existsByIdAndIsDeletedFalse(teamId)).willReturn(false);

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

            TeamMember teamMember = TeamMember.builder()
                    .teamId(teamId)
                    .userId(userId)
                    .role(TeamRole.MEMBER)
                    .build();

            given(teamRepo.existsByIdAndIsDeletedFalse(teamId)).willReturn(true);
            given(teamMemberRepo.findByTeamIdAndUserIdAndIsDeletedFalse(teamId, userId))
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
            Long applicationId = 100L;
            Long userId = 1L;

            TeamMember teamMember = TeamMember.builder()
                    .teamId(teamId)
                    .userId(userId)
                    .role(TeamRole.LEADER)
                    .build();

            TeamApplication application = TeamApplication.builder()
                    .id(applicationId)
                    .teamId(teamId)
                    .userId(2L)
                    .status(JoinStatus.REJECTED)
                    .build();

            given(teamRepo.existsByIdAndIsDeletedFalse(teamId)).willReturn(true);
            given(teamMemberRepo.findByTeamIdAndUserIdAndIsDeletedFalse(teamId, userId))
                    .willReturn(Optional.of(teamMember));
            given(teamJoinRequestRepo.findByIdAndTeamId(applicationId, teamId))
                    .willReturn(Optional.of(application));

            // when
            // then
            assertThatThrownBy(() -> teamService.rejectTeamApplication(teamId, applicationId, userId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이미 처리된 가입 신청입니다.");
        }
    }
}
