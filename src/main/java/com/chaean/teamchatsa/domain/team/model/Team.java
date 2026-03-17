package com.chaean.teamchatsa.domain.team.model;

import com.chaean.teamchatsa.domain.team.converter.TeamLevelConverter;
import com.chaean.teamchatsa.global.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "team")
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

	@Enumerated(EnumType.STRING)
	@NotNull
	@Column(name = "contact_type", nullable = false, length = 30)
	private ContactType contactType;

	@Size(max = 50)
	@NotNull
	@Column(name = "contact", nullable = false, length = 50)
	private String contact;

	@NotNull
	@Convert(converter = TeamLevelConverter.class)
	@Column(name = "level", nullable = false)
	private TeamLevel level;

	@Builder.Default
	@NotNull
	@Column(name = "win_rate", nullable = false)
	private Double winRate = 0.0;

	@Size(max = 255)
	@Column(name = "img")
	private String img;

	@JdbcTypeCode(SqlTypes.VECTOR)
	@Column(name = "style_vector", columnDefinition = "vector(384)")
	private float[] styleVector;

	public static Team create(
			Long leaderUserId,
			String name,
			String area,
			String description,
			ContactType contactType,
			String contact,
			TeamLevel level,
			String img
	) {
		return Team.builder()
				.leaderUserId(leaderUserId)
				.name(name)
				.area(area)
				.description(description)
				.contactType(contactType)
				.contact(contact)
				.level(level)
				.img(img)
				.winRate(0.0)
				.build();
	}

	public void updateWinRate(double winRate) {
		this.winRate = winRate;
	}
}
