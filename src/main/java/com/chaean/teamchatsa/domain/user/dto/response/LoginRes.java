package com.chaean.teamchatsa.domain.user.dto.response;

public record LoginRes(
		String accessToken,
		String refreshToken
) {
}
