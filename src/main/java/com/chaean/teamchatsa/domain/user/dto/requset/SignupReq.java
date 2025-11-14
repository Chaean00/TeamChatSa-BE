package com.chaean.teamchatsa.domain.user.dto.requset;

import com.chaean.teamchatsa.domain.team.model.Position;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupReq {
	@NotNull
	private String userName;
	@NotNull
	private String email;
	@NotNull
	private String password;
	private String phone;
	@NotNull
	private Position position;
}
