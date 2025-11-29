package com.chaean.teamchatsa.infra.slack;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlackAlertService {

	private final RestClient restClient;
	private final ObjectMapper objectMapper;

	@Value("${slack.webhook.url}")
	private String webhookUrl;

	@Value("${slack.webhook.enabled}")
	private boolean enabled;

	/** Slack 알림 전송 (비동기)*/
	@Async
	public void sendAlert(SlackMessage message) {
		if (!enabled || webhookUrl == null || webhookUrl.isEmpty()) {
			log.debug("Slack 알림이 비활성화되어 있습니다.");
			return;
		}

		try {
			String payload = objectMapper.writeValueAsString(message);

			String response = restClient.post()
					.uri(webhookUrl)
					.contentType(MediaType.APPLICATION_JSON)
					.body(payload)
					.retrieve()
					.body(String.class);

			log.info("Slack 알림 전송 성공: response={}", response);
		} catch (JsonProcessingException e) {
			log.error("Slack 메시지 JSON 변환 실패", e);
		} catch (Exception e) {
			log.error("Slack 알림 전송 실패", e);
		}
	}

	/** 에러 알림 전송 */
	public void sendErrorAlert(
			String errorTitle,
			String errorMessage,
			String stackTrace,
			String endpoint,
			String userId
	) {
		SlackMessage message = SlackMessage.createErrorAlert(
				errorTitle, errorMessage, stackTrace, endpoint, userId
		);
		sendAlert(message);
	}

	/** 비동기 작업 실패 알림 전송 (편의 메서드) */
	public void sendAsyncFailureAlert(String methodName, String errorMessage, Object[] params) {
		SlackMessage message = SlackMessage.createAsyncFailureAlert(
				methodName, errorMessage, params
		);
		sendAlert(message);
	}
}
