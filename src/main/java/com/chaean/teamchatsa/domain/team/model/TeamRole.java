package com.chaean.teamchatsa.domain.team.model;

public enum TeamRole {
	MEMBER("멤버"),
	CO_LEADER("부팀장"),
	LEADER("팀장");

	private final String description;

	TeamRole(String description) {
		this.description = description;
	}
}
