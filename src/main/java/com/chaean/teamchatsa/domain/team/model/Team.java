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
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "team")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE team SET deleted_at = NOW() WHERE id = ?")
public class Team extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@NotNull
	@Column(name = "leader_user_id", nullable = false)
	private Long leaderUserId;

	@Size(max = 50)
	@NotNull
	@Column(name = "name", nullable = false, length = 50)
	private String name;

	@Size(max = 50)
	@NotNull
	@Column(name = "area", nullable = false, length = 50)
	private String area;

	@Column(name = "description")
	private String description;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "contact_type", nullable = false, length = 30)
	private ContactType contactType;

	@Size(max = 50)
	@NotNull
	@Column(name = "contact", nullable = false, length = 50)
	private String contact;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "level", nullable = false, length = 20)
	private TeamLevel level;

	@Builder.Default
	@Column(name = "win_rate", nullable = false)
	private Double winRate = 0.0;

	@Size(max = 255)
	@Column(name = "img")
	private String img;

	@JdbcTypeCode(SqlTypes.VECTOR)
	@Column(name = "style_vector", columnDefinition = "vector(512)")
	private float[] styleVector;

	public static Team create(
			Long leaderUserId,
			String name,
			String area,
			String description,
			ContactType contactType,
			String contact,
			String img
	) {
		return Team.builder()
				.leaderUserId(leaderUserId)
				.name(name)
				.area(area)
				.description(description)
				.contactType(contactType)
				.contact(contact)
				.img(img)
				.build();
	}
}