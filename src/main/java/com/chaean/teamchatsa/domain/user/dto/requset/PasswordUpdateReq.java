package com.chaean.teamchatsa.domain.user.dto.requset;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordUpdateReq {
	@NotNull
	private String currentPassword;
	@NotNull
	private String newPassword;
}
