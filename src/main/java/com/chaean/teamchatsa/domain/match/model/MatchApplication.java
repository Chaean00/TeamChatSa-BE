package com.chaean.teamchatsa.domain.match.model;

import com.chaean.teamchatsa.global.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "match_application", uniqueConstraints = {
		@UniqueConstraint(name = "unique_match_post_team", columnNames = {"post_id", "applicant_team_id"})
})
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE match_application SET deleted_at = NOW() WHERE id = ?")
public class MatchApplication extends BaseEntity {

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

	public static MatchApplication create(Long postId, Long applicantTeamId, String message) {
		return MatchApplication.builder()
				.postId(postId)
				.applicantTeamId(applicantTeamId)
				.message(message)
				.build();
	}

	public void updateStatus(MatchApplicationStatus status) {
		this.status = status;
	}
}