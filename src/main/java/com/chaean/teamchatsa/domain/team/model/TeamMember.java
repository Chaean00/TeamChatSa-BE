package com.chaean.teamchatsa.domain.team.model;

import com.chaean.teamchatsa.global.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "team_member")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE team_member SET deleted_at = NOW() WHERE id = ?")
public class TeamMember extends BaseEntity {

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
	@Column(name = "role", nullable = false, length = 20)
	@Enumerated(EnumType.STRING)
	private TeamRole role = TeamRole.MEMBER;

	public static TeamMember of(Long teamId, Long userId, TeamRole role) {
		TeamMember member = new TeamMember();
		member.teamId = teamId;
		member.userId = userId;
		member.role = role;
		return member;
	}

	public void updateRole(TeamRole role) {
		this.role = role;
	}
}