package com.chaean.teamchatsa.domain.team.model;

import com.chaean.teamchatsa.global.common.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "team_review")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE team_review SET deleted_at = NOW() WHERE id = ?")
public class TeamReview extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @NotNull
    @Column(name = "reviewer_user_id", nullable = false)
    private Long reviewerUserId;

    @NotNull
    @Column(name = "match_id", nullable = false)
    private Long matchId;

    @Min(1) @Max(5)
    @Column(name = "rating", nullable = false)
    private int rating;

    @NotNull
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
}
