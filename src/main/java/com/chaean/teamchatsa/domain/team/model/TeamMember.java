package com.chaean.teamchatsa.domain.team.model;

import com.chaean.teamchatsa.global.common.model.DeleteAndTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "team_member")
public class TeamMember extends DeleteAndTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull
    @Builder.Default
    @Column(name = "role", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TeamRole role = TeamRole.MEMBER;

    public void updateRole(TeamRole role) {
        this.role = role;
    }
}