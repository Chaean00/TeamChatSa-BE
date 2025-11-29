package com.chaean.teamchatsa.domain.notification.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

	TEAM_APPLICATION("팀 가입 신청", "새로운 팀 가입 신청이 있습니다."),
	TEAM_APPLICATION_ACCEPTED("팀 가입 수락", "팀 가입 신청이 수락되었습니다."),
	TEAM_APPLICATION_REJECTED("팀 가입 거절", "팀 가입 신청이 거절되었습니다."),

	MATCH_APPLICATION("매치 신청", "새로운 매치 신청이 있습니다."),
	MATCH_APPLICATION_ACCEPTED("매치 수락", "매치 신청이 수락되었습니다."),
	MATCH_APPLICATION_REJECTED("매치 거절", "매치 신청이 거절되었습니다.");

	private final String title;
	private final String description;
}
