package com.chaean.teamchatsa.domain.user.dto.requset;

import com.chaean.teamchatsa.domain.team.model.Position;
import jakarta.validation.constraints.NotNull;

public record SignupReq(
		@NotNull
		String userName,
		@NotNull
		String email,
		@NotNull
		String password,
		String phone,
		@NotNull
		Position position
) {
}
