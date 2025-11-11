package com.chaean.teamchatsa.domain.user.dto.requset;

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
