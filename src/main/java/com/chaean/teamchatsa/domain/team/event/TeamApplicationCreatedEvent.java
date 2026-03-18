package com.chaean.teamchatsa.domain.team.event;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

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
