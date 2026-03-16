package com.chaean.teamchatsa.domain.notification.listener;

import com.chaean.teamchatsa.domain.match.event.MatchApplicationCreatedEvent;
import com.chaean.teamchatsa.domain.match.event.MatchApplicationProcessedEvent;
import com.chaean.teamchatsa.domain.notification.service.NotificationEventService;
import com.chaean.teamchatsa.domain.team.event.TeamApplicationCreatedEvent;
import com.chaean.teamchatsa.domain.team.event.TeamApplicationProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 알림 이벤트 리스너
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

	private final NotificationEventService notificationEventService;

	/**
	 * 팀 가입 신청 이벤트 처리 수신 대상: 팀장 및 부팀장에게 팀 가입 신청 알림 발송
	 */
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleTeamApplicationCreated(TeamApplicationCreatedEvent event) {
		notificationEventService.handleTeamApplicationCreated(event);
	}

	/**
	 * 팀 가입 신청 처리 이벤트 (수락/거절) 신청자에게 가입 신청 수락/거절 알림 발송
	 */
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleTeamApplicationProcessed(TeamApplicationProcessedEvent event) {
		notificationEventService.handleTeamApplicationProcessed(event);
	}

	/**
	 * 매치 신청 이벤트 처리 매치 게시물 작성 팀의 팀장/부팀장에게 매치 신청 알림 발송
	 */
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleMatchApplicationCreated(MatchApplicationCreatedEvent event) {
		notificationEventService.handleMatchApplicationCreated(event);
	}

	/**
	 * 매치 신청 처리 이벤트 (수락/거절) 신청 팀의 팀장/부팀장에게 수락/거절 알림 발송
	 */
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleMatchApplicationProcessed(MatchApplicationProcessedEvent event) {
		notificationEventService.handleMatchApplicationProcessed(event);
	}
}
