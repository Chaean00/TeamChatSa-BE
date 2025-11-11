package com.chaean.teamchatsa.domain.team.model;

import lombok.Getter;

@Getter
public enum ContactType {
	KAKAO("카카오톡 ID"),
	PHONE("전화번호"),
	EMAIL("이메일");

	private final String description;

	ContactType(String description) {
		this.description = description;
	}
}
