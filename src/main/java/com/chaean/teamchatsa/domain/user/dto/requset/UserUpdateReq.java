package com.chaean.teamchatsa.domain.user.dto.requset;

import com.chaean.teamchatsa.domain.team.model.Position;

public record UserUpdateReq(
		String nickname,
		Position position,
		String phone
) {
}
