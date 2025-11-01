package com.chaean.teamchatsa.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record LoginReq(
		@Email
		@NotNull
		String email,
		@NotNull
		String password
) {
}
