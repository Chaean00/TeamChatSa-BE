package com.chaean.teamchatsa.domain.team.controller;

import com.chaean.teamchatsa.domain.team.model.ContactType;
import com.chaean.teamchatsa.domain.team.model.Team;
import com.chaean.teamchatsa.domain.team.model.TeamMember;
import com.chaean.teamchatsa.domain.team.model.TeamRole;
import com.chaean.teamchatsa.domain.team.repository.TeamMemberRepository;
import com.chaean.teamchatsa.domain.team.repository.TeamRepository;
import com.chaean.teamchatsa.domain.team.service.TeamService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private TeamService teamService;

    private Team testTeam;
    private Long leaderUserId;
    private Long coLeaderUserId;
    private Long memberUserId;
    private Long nonMemberUserId;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        leaderUserId = 1L;
        coLeaderUserId = 2L;
        memberUserId = 3L;
        nonMemberUserId = 999L;

        // 팀 생성
        testTeam = Team.builder()
                .leaderUserId(leaderUserId)
                .name("테스트 팀")
                .area("서울")
                .description("테스트용 팀입니다")
                .contactType(ContactType.KAKAO)
                .contact("test-contact")
                .img("test-image.jpg")
                .build();
        teamRepository.save(testTeam);

        // 팀 멤버 생성 (LEADER)
        TeamMember leader = TeamMember.builder()
                .teamId(testTeam.getId())
                .userId(leaderUserId)
                .role(TeamRole.LEADER)
                .build();
        teamMemberRepository.save(leader);

        // 팀 멤버 생성 (CO_LEADER)
        TeamMember coLeader = TeamMember.builder()
                .teamId(testTeam.getId())
                .userId(coLeaderUserId)
                .role(TeamRole.CO_LEADER)
                .build();
        teamMemberRepository.save(coLeader);

        // 팀 멤버 생성 (MEMBER)
        TeamMember member = TeamMember.builder()
                .teamId(testTeam.getId())
                .userId(memberUserId)
                .role(TeamRole.MEMBER)
                .build();
        teamMemberRepository.save(member);
    }

    @Nested
    @DisplayName("팀 삭제 API - DELETE /api/v1/teams/{teamId}")
    class DeleteTeam {

        @Test
        @DisplayName("성공 - LEADER가 멤버 1명만 있는 팀 삭제")
        void success_leader_delete_team_with_only_leader() throws Exception {
            // given
            Team soloTeam = Team.builder()
                    .leaderUserId(leaderUserId)
                    .name("솔로 팀")
                    .area("부산")
                    .description("혼자만 있는 팀")
                    .contactType(ContactType.KAKAO)
                    .contact("solo-contact")
                    .build();
            teamRepository.save(soloTeam);

            TeamMember soloLeader = TeamMember.builder()
                    .teamId(soloTeam.getId())
                    .userId(leaderUserId)
                    .role(TeamRole.LEADER)
                    .build();
            teamMemberRepository.save(soloLeader);

            setSecurityContext(leaderUserId);

            // when & then
            mockMvc.perform(delete("/api/v1/teams/{teamId}", soloTeam.getId()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("팀이 삭제되었습니다."));
        }

        @Test
        @DisplayName("실패 - CO_LEADER는 팀 삭제 불가 (권한 부족)")
        void fail_co_leader_cannot_delete_team() throws Exception {
            // given
            Team soloTeam = createSoloTeam(leaderUserId);
            setSecurityContext(coLeaderUserId); // CO_LEADER로 인증

            // when & then
            mockMvc.perform(delete("/api/v1/teams/{teamId}", soloTeam.getId()))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - MEMBER는 팀 삭제 불가 (권한 부족)")
        void fail_member_cannot_delete_team() throws Exception {
            // given
            Team soloTeam = createSoloTeam(leaderUserId);
            setSecurityContext(memberUserId); // MEMBER로 인증

            // when & then
            mockMvc.perform(delete("/api/v1/teams/{teamId}", soloTeam.getId()))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 팀 멤버가 아닌 사용자는 팀 삭제 불가")
        void fail_non_member_cannot_delete_team() throws Exception {
            // given
            Team soloTeam = createSoloTeam(leaderUserId);
            setSecurityContext(nonMemberUserId); // 팀 멤버가 아닌 사용자

            // when & then
            mockMvc.perform(delete("/api/v1/teams/{teamId}", soloTeam.getId()))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 팀원이 2명 이상인 경우 삭제 불가")
        void fail_cannot_delete_team_with_multiple_members() throws Exception {
            // given
            setSecurityContext(leaderUserId); // LEADER로 인증

            // when & then (testTeam은 3명의 멤버가 있음)
            mockMvc.perform(delete("/api/v1/teams/{teamId}", testTeam.getId()))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("팀원 존재로 인해 팀을 삭제할 수 없습니다."));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 팀 삭제 시도")
        void fail_delete_non_existent_team() throws Exception {
            // given
            Long nonExistentTeamId = 99999L;
            setSecurityContext(leaderUserId);

            // when & then
            mockMvc.perform(delete("/api/v1/teams/{teamId}", nonExistentTeamId))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("존재하지 않는 팀입니다."));
        }

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자")
        void fail_unauthenticated_user() throws Exception {
            // given
            Team soloTeam = createSoloTeam(leaderUserId);
            SecurityContextHolder.clearContext(); // 인증 정보 제거

            // when & then
            mockMvc.perform(delete("/api/v1/teams/{teamId}", soloTeam.getId()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    /**
     * SecurityContext에 userId를 설정하는 헬퍼 메서드
     */
    private void setSecurityContext(Long userId) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * 리더 1명만 있는 팀을 생성하는 헬퍼 메서드
     */
    private Team createSoloTeam(Long leaderId) {
        Team soloTeam = Team.builder()
                .leaderUserId(leaderId)
                .name("솔로 팀")
                .area("대전")
                .description("혼자만 있는 팀")
                .contactType(ContactType.PHONE)
                .contact("010-1234-5678")
                .build();
        teamRepository.save(soloTeam);

        TeamMember soloLeader = TeamMember.builder()
                .teamId(soloTeam.getId())
                .userId(leaderId)
                .role(TeamRole.LEADER)
                .build();
        teamMemberRepository.save(soloLeader);

        return soloTeam;
    }
}
