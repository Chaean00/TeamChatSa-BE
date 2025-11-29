package com.chaean.teamchatsa.domain.team.event;

import com.chaean.teamchatsa.domain.team.model.JoinStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 팀 가입 신청 처리 이벤트 (승인/거절)
 */
@Getter
@AllArgsConstructor
public class TeamApplicationProcessedEvent {
	private final Long applicantUserId;
	private final String teamName;
	private final JoinStatus status;  // ACCEPTED or REJECTED
	private final LocalDateTime occurredAt;
}
