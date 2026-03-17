package com.chaean.teamchatsa.domain.team.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 팀 리뷰 임베딩 이벤트
 */
@Getter
@AllArgsConstructor
public class TeamReviewCreatedEvent {

	private final Long teamId;
}
