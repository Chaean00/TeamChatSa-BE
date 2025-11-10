package com.chaean.teamchatsa.domain.user.dto.response;

public record TokenRes(
        String accessToken,
		String refreshToken
) {
}