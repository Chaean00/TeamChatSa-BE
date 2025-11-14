package com.chaean.teamchatsa.domain.user.dto.requset;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginReq {
	@Email
	@NotNull
	private String email;
	@NotNull
	private String password;
}
