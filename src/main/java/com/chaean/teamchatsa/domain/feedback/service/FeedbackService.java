package com.chaean.teamchatsa.domain.feedback.service;

import com.chaean.teamchatsa.domain.feedback.dto.request.FeedbackCreateRequest;
import com.chaean.teamchatsa.domain.feedback.model.Feedback;
import com.chaean.teamchatsa.domain.feedback.repository.FeedbackRepository;
import com.chaean.teamchatsa.domain.user.model.User;
import com.chaean.teamchatsa.domain.user.repository.UserRepository;
import com.chaean.teamchatsa.global.common.aop.annotation.Loggable;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import com.chaean.teamchatsa.infra.slack.SlackAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackService {

	private final FeedbackRepository feedbackRepository;
	private final UserRepository userRepository;
	private final SlackAlertService slackAlertService;

	@Transactional
	@Loggable
	public void createFeedback(Long userId, FeedbackCreateRequest request) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "유저 정보를 찾을 수 없습니다."));

		String senderName = StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
		Feedback feedback = Feedback.create(user.getId(), senderName, request.getContent().trim());

		feedbackRepository.save(feedback);
		slackAlertService.sendFeedbackAlert(user.getId(), senderName, feedback.getContent());
		log.info("개발자 의견 저장 완료: senderUserId={}", userId);
	}
}
