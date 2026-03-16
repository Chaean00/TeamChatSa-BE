package com.chaean.teamchatsa.domain.user.dto.requset;

import com.chaean.teamchatsa.domain.team.model.Position;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

	private String nickname;

	private Position position;

	private String phone;
}
