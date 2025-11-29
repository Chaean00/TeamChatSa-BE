package com.chaean.teamchatsa.domain.match.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 매치 신청 생성 이벤트
 */
@Getter
@AllArgsConstructor
public class MatchApplicationCreatedEvent {
	private final Long matchId;
	private final Long postOwnerTeamId;  // 매치 게시물 작성 팀 ID
	private final Long applicantTeamId;  // 신청 팀 ID
	private final String applicantTeamName;
	private final LocalDateTime occurredAt;
}
