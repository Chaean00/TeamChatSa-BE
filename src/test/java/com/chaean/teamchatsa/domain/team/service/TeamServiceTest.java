package com.chaean.teamchatsa.domain.team.service;

import com.chaean.teamchatsa.domain.team.dto.request.TeamCreateReq;
import com.chaean.teamchatsa.domain.team.dto.request.TeamJoinReq;
import com.chaean.teamchatsa.domain.team.dto.response.TeamDetailRes;
import com.chaean.teamchatsa.domain.team.dto.response.TeamListRes;
import com.chaean.teamchatsa.domain.team.model.ContactType;
import com.chaean.teamchatsa.domain.team.model.Team;
import com.chaean.teamchatsa.domain.team.repository.TeamJoinRequestRepository;
import com.chaean.teamchatsa.domain.team.repository.TeamMemberRepository;
import com.chaean.teamchatsa.domain.team.repository.TeamRepository;
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
            TeamCreateReq req = new TeamCreateReq("teamName", "area", "description", ContactType.KAKAO, "contact", "imgUrl");
            given(teamMemberRepo.existsByUserIdAndIsDeletedFalse(userId)).willReturn(false);
            given(teamRepo.existsByLeaderUserIdAndIsDeletedFalse(userId)).willReturn(false);

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
            TeamCreateReq req = new TeamCreateReq("teamName", "area", "description", ContactType.KAKAO, "contact", "imgUrl");
            given(teamMemberRepo.existsByUserIdAndIsDeletedFalse(userId)).willReturn(true);

            // when
            // then
            assertThatThrownBy(() -> teamService.registerTeam(userId, req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이미 가입한 팀이 존재합니다.");
        }

        @Test
        @DisplayName("실패 - 이미 리더인 팀이 존재")
        void fail_already_leader() {
            // given
            Long userId = 1L;
            TeamCreateReq req = new TeamCreateReq("teamName", "area", "description", ContactType.KAKAO, "contact", "imgUrl");
            given(teamMemberRepo.existsByUserIdAndIsDeletedFalse(userId)).willReturn(false);
            given(teamRepo.existsByLeaderUserIdAndIsDeletedFalse(userId)).willReturn(true);

            // when
            // then
            assertThatThrownBy(() -> teamService.registerTeam(userId, req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이미 리더로 존재하는 팀이 존재합니다.");
        }
    }

    @Nested
    @DisplayName("팀 목록 조회")
    class FindTeamList {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            int page = 0;
            int size = 10;
            Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
            Slice<TeamListRes> expected = new SliceImpl<>(Collections.emptyList());
            given(teamRepo.findTeamListWithPagination(pageable)).willReturn(expected);

            // when
            Slice<TeamListRes> result = teamService.findTeamList(page, size);

            // then
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("팀 상세 조회")
    class FindTeamDetail {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            Long teamId = 1L;
            Team team = Team.builder()
                    .id(teamId)
                    .name("teamName")
                    .build();
            given(teamRepo.findByIdAndIsDeletedFalse(teamId)).willReturn(Optional.of(team));
            given(teamMemberRepo.countByTeamIdAndIsDeletedFalse(teamId)).willReturn(1L);

            // when
            TeamDetailRes result = teamService.findTeamDetail(teamId);

            // then
            assertThat(result.name()).isEqualTo("teamName");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 팀")
        void fail_not_found() {
            // given
            Long teamId = 1L;
            given(teamRepo.findByIdAndIsDeletedFalse(teamId)).willReturn(Optional.empty());

            // when
            // then
            assertThatThrownBy(() -> teamService.findTeamDetail(teamId))
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
}
