package com.chaean.teamchatsa.domain.team.dto.response;

import com.chaean.teamchatsa.domain.team.model.JoinStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TeamApplicationResponse {

	private Long applicationId;
	private Long userId;
	private String userName;
	private String userNickname;
	private String message;
	private JoinStatus status;
	private LocalDateTime appliedAt;
}