package com.chaean.teamchatsa.domain.user.dto.response;

import com.chaean.teamchatsa.domain.team.model.Position;
import lombok.Builder;

@Builder
public record UserRes(
		Long id,
		String email,
		String name,
		String phone,
		Position position,
		String nickname,
		boolean isLocalAccount
) {
}
