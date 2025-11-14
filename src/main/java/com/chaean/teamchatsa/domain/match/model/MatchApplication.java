package com.chaean.teamchatsa.domain.match.model;

import com.chaean.teamchatsa.global.common.model.TimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "match_application", uniqueConstraints = {
        @UniqueConstraint(name = "unique_match_post_team", columnNames = {"post_id", "applicant_team_id"})
})
public class MatchApplication extends TimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "post_id", nullable = false)
    private Long postId;

    @NotNull
    @Column(name = "applicant_team_id", nullable = false)
    private Long applicantTeamId;

    @Column(name = "message", length = Integer.MAX_VALUE)
    private String message;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MatchApplicationStatus status = MatchApplicationStatus.PENDING;

    public void updateStatus(MatchApplicationStatus status) {
        this.status = status;
    }
}