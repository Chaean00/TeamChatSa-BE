package com.chaean.teamchatsa.domain.team.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 팀 가입 신청 생성 이벤트
 */
@Getter
@AllArgsConstructor
public class TeamApplicationCreatedEvent {
	private final Long teamId;
	private final Long applicantUserId;
	private final String applicantNickname;
	private final Long applicationId;
	private final String message;
	private final LocalDateTime occurredAt;
}
