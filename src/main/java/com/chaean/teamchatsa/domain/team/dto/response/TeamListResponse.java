package com.chaean.teamchatsa.domain.team.dto.response;

import com.chaean.teamchatsa.domain.team.model.TeamLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TeamListResponse {

	private Long id;
	private String name;
	private String area;
	private String img;
	private String description;
	private Long memberCount;
	private Integer level;
	private String levelLabel;

	public TeamListResponse(
			Long id,
			String name,
			String area,
			String img,
			String description,
			Long memberCount,
			TeamLevel level
	) {
		this.id = id;
		this.name = name;
		this.area = area;
		this.img = img;
		this.description = description;
		this.memberCount = memberCount;
		this.level = level != null ? level.getValue() : null;
		this.levelLabel = level != null ? level.getDescription() : null;
	}
}
