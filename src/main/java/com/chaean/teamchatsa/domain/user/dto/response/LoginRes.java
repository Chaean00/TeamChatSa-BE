package com.chaean.teamchatsa.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRes {
	private String accessToken;
	private String refreshToken;
}
