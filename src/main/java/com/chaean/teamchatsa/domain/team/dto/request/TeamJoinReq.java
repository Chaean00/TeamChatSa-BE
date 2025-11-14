package com.chaean.teamchatsa.domain.team.dto.request;

import jakarta.validation.constraints.NotNull;

public record TeamJoinReq(
		@NotNull
		String message
) {
}
