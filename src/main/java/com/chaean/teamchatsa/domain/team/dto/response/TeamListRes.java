package com.chaean.teamchatsa.domain.team.dto.response;

public record TeamListRes(
		Long id,
		String name,
		String area,
		String img,
		String description,
		Long memberCount
) {
}
