package com.chaean.teamchatsa.domain.team.listener;

import com.chaean.teamchatsa.domain.team.event.TeamReviewCreatedEvent;
import com.chaean.teamchatsa.domain.team.service.TeamAiEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamAiEventListener {

	private final TeamAiEventService teamAiEventService;

	/**
	 * 팀 리뷰 생성 시 AI 스타일 벡터 갱신 이벤트 핸들러
	 */
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleTeamAiEvent(TeamReviewCreatedEvent event) {
		teamAiEventService.handleTeamAiEvent(event);
	}
}
