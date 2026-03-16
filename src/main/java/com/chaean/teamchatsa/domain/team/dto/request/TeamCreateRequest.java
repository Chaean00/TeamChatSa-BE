package com.chaean.teamchatsa.domain.team.dto.request;

import com.chaean.teamchatsa.domain.team.model.ContactType;
import com.chaean.teamchatsa.domain.team.model.TeamLevel;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TeamCreateRequest {

	@NotNull
	private String name;

	@NotNull
	private String area;

	private String description;

	@NotNull
	private ContactType contactType;

	@NotNull
	private String contact;

	private String imgUrl;

	@NotNull
	private TeamLevel level;
}
