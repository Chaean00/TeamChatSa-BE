package com.chaean.teamchatsa.domain.match.model;

import com.chaean.teamchatsa.global.common.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "match_result")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE match_result SET deleted_at = NOW() WHERE id = ?")
public class MatchResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "match_post_id", nullable = false)
    private Long matchPostId;

    @NotNull
    @Column(name = "home_team_id", nullable = false)
    private Long homeTeamId;

    @NotNull
    @Column(name = "away_team_id", nullable = false)
    private Long awayTeamId;

    @Column(name = "winner_team_id")
    private Long winnerTeamId; // 무승부일 경우 null

    @Column(name = "home_score")
    private Integer homeScore;

    @Column(name = "away_score")
    private Integer awayScore;
}
