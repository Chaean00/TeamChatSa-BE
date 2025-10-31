package com.chaean.teamchatsa.domain.user.dto;

import com.chaean.teamchatsa.domain.user.model.UserRole;
import jakarta.validation.constraints.NotNull;

public record SignupReq(
		@NotNull
		String userName,
		String email,
		@NotNull
		String password,
		@NotNull
		UserRole role
) {
}
