package com.chaean.teamchatsa.domain.notification.service;

import com.chaean.teamchatsa.domain.match.event.MatchApplicationCreatedEvent;
import com.chaean.teamchatsa.domain.match.event.MatchApplicationProcessedEvent;
import com.chaean.teamchatsa.domain.match.model.MatchApplicationStatus;
import com.chaean.teamchatsa.domain.notification.model.NotificationType;
import com.chaean.teamchatsa.domain.team.event.TeamApplicationCreatedEvent;
import com.chaean.teamchatsa.domain.team.event.TeamApplicationProcessedEvent;
import com.chaean.teamchatsa.domain.team.model.JoinStatus;
import com.chaean.teamchatsa.domain.team.model.TeamMember;
import com.chaean.teamchatsa.domain.team.model.TeamRole;
import com.chaean.teamchatsa.domain.team.repository.TeamMemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventService {

	private final NotificationService notificationService;
	private final TeamMemberRepository teamMemberRepo;

	@Transactional
	public void handleTeamApplicationCreated(TeamApplicationCreatedEvent event) {
		log.info("팀 가입 신청 이벤트 처리: teamId={}, applicantId={}",
				event.getTeamId(), event.getApplicantUserId());

		// 팀 리더/부리더 조회
		List<TeamMember> leaders = teamMemberRepo.findByTeamIdAndRoleIn(
				event.getTeamId(),
				List.of(TeamRole.LEADER, TeamRole.CO_LEADER)
		);

		// 알림 수신자 ID 목록 변환
		List<Long> leaderUserIds = leaders.stream()
				.map(TeamMember::getUserId)
				.distinct()
				.toList();

		if (leaderUserIds.isEmpty()) {
			log.warn("팀 가입 신청 알림 대상 없음: teamId={}", event.getTeamId());
			return;
		}
		
		// 팀 가입 신청 알림 일괄 저장
		notificationService.createNotifications(
				leaderUserIds,
				NotificationType.TEAM_APPLICATION,
				event.getApplicantNickname() + "님이 팀 가입을 신청했습니다.",
				"/mypage"
		);

		log.info("팀 가입 신청 알림 배치 저장 완료: count={}", leaderUserIds.size());
	}

	@Transactional
	public void handleTeamApplicationProcessed(TeamApplicationProcessedEvent event) {
		log.info("팀 가입 처리 이벤트: applicantId={}, status={}",
				event.getApplicantUserId(), event.getStatus());

		// 처리 결과별 알림 타입 결정
		NotificationType type = event.getStatus() == JoinStatus.ACCEPTED
				? NotificationType.TEAM_APPLICATION_ACCEPTED
				: NotificationType.TEAM_APPLICATION_REJECTED;

		// 처리 결과 알림 문구 생성
		String content = event.getStatus() == JoinStatus.ACCEPTED
				? event.getTeamName() + " 팀 가입이 승인되었습니다."
				: event.getTeamName() + " 팀 가입이 거절되었습니다.";

		// 팀 가입 신청자에게 알림으로 결과 전송
		notificationService.createNotification(
				event.getApplicantUserId(),
				type,
				content,
				"/mypage"
		);
	}

	@Transactional
	public void handleMatchApplicationCreated(MatchApplicationCreatedEvent event) {
		log.info("매치 신청 이벤트 처리: matchId={}, applicantTeamId={}",
				event.getMatchId(), event.getApplicantTeamId());

		// 게시글 소유 팀 리더/부리더 조회
		List<TeamMember> leaders = teamMemberRepo.findByTeamIdAndRoleIn(
				event.getPostOwnerTeamId(),
				List.of(TeamRole.LEADER, TeamRole.CO_LEADER)
		);

		// 알림 수신자 ID 목록 변환
		List<Long> leaderUserIds = leaders.stream()
				.map(TeamMember::getUserId)
				.toList();

		// 매치 신청 알림 일괄 저장
		notificationService.createNotifications(
				leaderUserIds,
				NotificationType.MATCH_APPLICATION,
				event.getApplicantTeamName() + " 팀이 매치를 신청했습니다.",
				"/matches/" + event.getMatchId()
		);

		log.info("매치 신청 알림 배치 저장 완료: count={}", leaderUserIds.size());
	}

	@Transactional
	public void handleMatchApplicationProcessed(MatchApplicationProcessedEvent event) {
		log.info("매치 처리 이벤트: applicantTeamId={}, status={}",
				event.getApplicantTeamId(), event.getStatus());

		// 처리 결과별 알림 타입 결정
		NotificationType type = event.getStatus() == MatchApplicationStatus.ACCEPTED
				? NotificationType.MATCH_APPLICATION_ACCEPTED
				: NotificationType.MATCH_APPLICATION_REJECTED;

		// 처리 결과 알림 문구 생성
		String content = event.getStatus() == MatchApplicationStatus.ACCEPTED
				? "\"" + event.getMatchTitle() + "\" 매치 신청이 승인되었습니다."
				: "\"" + event.getMatchTitle() + "\" 매치 신청이 거절되었습니다.";

		// 신청 팀 리더/부리더 조회
		List<TeamMember> leaders = teamMemberRepo.findByTeamIdAndRoleIn(
				event.getApplicantTeamId(),
				List.of(TeamRole.LEADER, TeamRole.CO_LEADER)
		);

		// 알림 수신자 ID 목록 변환
		List<Long> leaderUserIds = leaders.stream()
				.map(TeamMember::getUserId)
				.toList();

		// 매치 처리 결과 알림 일괄 저장
		notificationService.createNotifications(
				leaderUserIds,
				type,
				content,
				"/matches"
		);

		log.info("매치 처리 알림 배치 저장 완료: count={}", leaderUserIds.size());
	}
}
