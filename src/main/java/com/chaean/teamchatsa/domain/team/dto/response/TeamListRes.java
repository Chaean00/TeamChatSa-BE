package com.chaean.teamchatsa.domain.team.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TeamListRes {
	private Long id;
	private String name;
	private String area;
	private String img;
	private String description;
	private Long memberCount;
}
