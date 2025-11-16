package com.chaean.teamchatsa.domain.team.dto.response;

import com.chaean.teamchatsa.domain.team.model.JoinStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TeamApplicationRes {
	private Long applicationId;
	private Long userId;
	private String userName;
	private String userNickname;
	private String message;
	private JoinStatus status;
	private LocalDateTime appliedAt;
}