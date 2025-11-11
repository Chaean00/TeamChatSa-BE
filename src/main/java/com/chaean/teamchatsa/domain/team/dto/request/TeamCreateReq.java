package com.chaean.teamchatsa.domain.team.dto.request;

import com.chaean.teamchatsa.domain.team.model.ContactType;
import jakarta.validation.constraints.NotNull;

public record TeamCreateReq(
		@NotNull
		String name,
		@NotNull
		String area,
		String description,
		@NotNull
		ContactType contactType,
		@NotNull
		String contact,
		String imgUrl
) {
}
