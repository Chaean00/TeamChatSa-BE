package com.chaean.teamchatsa.domain.match.event;

import com.chaean.teamchatsa.domain.match.model.MatchApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 매치 신청 처리 이벤트 (승인/거절)
 */
@Getter
@AllArgsConstructor
public class MatchApplicationProcessedEvent {
	private final Long applicantTeamId;
	private final String matchTitle;
	private final MatchApplicationStatus status;  // ACCEPTED or REJECTED
	private final LocalDateTime occurredAt;
}
