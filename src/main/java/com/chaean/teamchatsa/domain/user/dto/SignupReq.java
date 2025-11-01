package com.chaean.teamchatsa.domain.user.dto;

import jakarta.validation.constraints.NotNull;

public record SignupReq(
		@NotNull
		String userName,
		String email,
		@NotNull
		String password,
		String phone
) {
}
