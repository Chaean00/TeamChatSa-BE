package com.chaean.teamchatsa.domain.notification.listener;

import com.chaean.teamchatsa.domain.match.model.MatchApplicationStatus;
import com.chaean.teamchatsa.domain.notification.model.NotificationType;
import com.chaean.teamchatsa.domain.notification.service.NotificationService;
import com.chaean.teamchatsa.domain.team.model.JoinStatus;
import com.chaean.teamchatsa.domain.team.model.TeamMember;
import com.chaean.teamchatsa.domain.team.model.TeamRole;
import com.chaean.teamchatsa.domain.team.repository.TeamMemberRepository;
import com.chaean.teamchatsa.domain.match.event.MatchApplicationCreatedEvent;
import com.chaean.teamchatsa.domain.match.event.MatchApplicationProcessedEvent;
import com.chaean.teamchatsa.domain.team.event.TeamApplicationCreatedEvent;
import com.chaean.teamchatsa.domain.team.event.TeamApplicationProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.stream.Collectors;

/** 알림 이벤트 리스너 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

	private final NotificationService notificationService;
	private final TeamMemberRepository teamMemberRepo;

	/**
	 * 팀 가입 신청 이벤트 처리
	 * 수신 대상: 팀장 및 부팀장에게 팀 가입 신청 알림 발송
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Async
	public void handleTeamApplicationCreated(TeamApplicationCreatedEvent event) {
		log.info("팀 가입 신청 이벤트 처리: teamId={}, applicantId={}",
				event.getTeamId(), event.getApplicantUserId());

		// 팀장 및 부팀장 조회
		List<TeamMember> leaders = teamMemberRepo.findByTeamIdAndRoleInAndIsDeletedFalse(
				event.getTeamId(),
				List.of(TeamRole.LEADER, TeamRole.CO_LEADER)
		);

		// 리더 userId 목록 추출
		List<Long> leaderUserIds = leaders.stream()
				.map(TeamMember::getUserId)
				.collect(Collectors.toList());

		// 배치로 한 번에 알림 생성 (N+1 해결)
		notificationService.createNotifications(
				leaderUserIds,
				NotificationType.TEAM_APPLICATION,
				event.getApplicantNickname() + "님이 팀 가입을 신청했습니다.",
				"/mypage"
		);

		log.info("팀 가입 신청 알림 배치 저장 완료: count={}", leaderUserIds.size());
	}

	/**
	 * 팀 가입 신청 처리 이벤트 (수락/거절)
	 * 신청자에게 가입 신청 수락/거절 알림 발송
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Async
	public void handleTeamApplicationProcessed(TeamApplicationProcessedEvent event) {
		log.info("팀 가입 처리 이벤트: applicantId={}, status={}",
				event.getApplicantUserId(), event.getStatus());

		NotificationType type = event.getStatus() == JoinStatus.ACCEPTED
				? NotificationType.TEAM_APPLICATION_ACCEPTED
				: NotificationType.TEAM_APPLICATION_REJECTED;

		String content = event.getStatus() == JoinStatus.ACCEPTED
				? event.getTeamName() + " 팀 가입이 승인되었습니다."
				: event.getTeamName() + " 팀 가입이 거절되었습니다.";

		notificationService.createNotification(
				event.getApplicantUserId(),
				type,
				content,
				"/mypage"
		);
	}

	/**
	 * 매치 신청 이벤트 처리
	 * 매치 게시물 작성 팀의 팀장/부팀장에게 매치 신청 알림 발송
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Async
	public void handleMatchApplicationCreated(MatchApplicationCreatedEvent event) {
		log.info("매치 신청 이벤트 처리: matchId={}, applicantTeamId={}",
				event.getMatchId(), event.getApplicantTeamId());

		// 매치 게시물 작성 팀의 팀장/부팀장 조회
		List<TeamMember> leaders = teamMemberRepo.findByTeamIdAndRoleInAndIsDeletedFalse(
				event.getPostOwnerTeamId(),
				List.of(TeamRole.LEADER, TeamRole.CO_LEADER)
		);

		// 리더 userId 목록 추출
		List<Long> leaderUserIds = leaders.stream()
				.map(TeamMember::getUserId)
				.collect(Collectors.toList());

		// 배치로 한 번에 알림 생성 (N+1 해결)
		notificationService.createNotifications(
				leaderUserIds,
				NotificationType.MATCH_APPLICATION,
				event.getApplicantTeamName() + " 팀이 매치를 신청했습니다.",
				"/matches/" + event.getMatchId()
		);

		log.info("매치 신청 알림 배치 저장 완료: count={}", leaderUserIds.size());
	}

	/**
	 * 매치 신청 처리 이벤트 (수락/거절)
	 * 신청 팀의 팀장/부팀장에게 수락/거절 알림 발송
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Async
	public void handleMatchApplicationProcessed(MatchApplicationProcessedEvent event) {
		log.info("매치 처리 이벤트: applicantTeamId={}, status={}",
				event.getApplicantTeamId(), event.getStatus());

		NotificationType type = event.getStatus() == MatchApplicationStatus.ACCEPTED
				? NotificationType.MATCH_APPLICATION_ACCEPTED
				: NotificationType.MATCH_APPLICATION_REJECTED;

		String content = event.getStatus() == MatchApplicationStatus.ACCEPTED
				? "\"" + event.getMatchTitle() + "\" 매치 신청이 승인되었습니다."
				: "\"" + event.getMatchTitle() + "\" 매치 신청이 거절되었습니다.";

		// 신청 팀의 팀장/부팀장 조회
		List<TeamMember> leaders = teamMemberRepo.findByTeamIdAndRoleInAndIsDeletedFalse(
				event.getApplicantTeamId(),
				List.of(TeamRole.LEADER, TeamRole.CO_LEADER)
		);

		// 리더 userId 목록 추출
		List<Long> leaderUserIds = leaders.stream()
				.map(TeamMember::getUserId)
				.collect(Collectors.toList());

		// 배치로 한 번에 알림 생성 (N+1 해결)
		notificationService.createNotifications(
				leaderUserIds,
				type,
				content,
				"/matches"
		);

		log.info("매치 처리 알림 배치 저장 완료: count={}", leaderUserIds.size());
	}
}
