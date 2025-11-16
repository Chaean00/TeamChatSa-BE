package com.chaean.teamchatsa.domain.user.dto.response;

import com.chaean.teamchatsa.domain.team.model.Position;
import com.chaean.teamchatsa.domain.team.model.TeamRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRes {
	private Long id;
	private String email;
	private String name;
	private String phone;
	private Position position;
	private String nickname;
	private boolean isLocalAccount;
	private Long teamId;
	private TeamRole teamRole;
}
