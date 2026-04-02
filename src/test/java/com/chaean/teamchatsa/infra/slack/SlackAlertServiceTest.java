package com.chaean.teamchatsa.infra.slack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.client.RestClient;

class SlackAlertServiceTest {

	@Test
	@DisplayName("활성 프로필이 있으면 Slack 환경 라벨에 활성 프로필을 사용한다")
	void resolveEnvironmentLabelUsesActiveProfiles() {
		MockEnvironment environment = new MockEnvironment();
		environment.setActiveProfiles("prod");
		SlackAlertService service = new SlackAlertService(
				mock(RestClient.class),
				new ObjectMapper(),
				environment
		);

		assertThat(service.resolveEnvironmentLabel()).isEqualTo("prod");
	}

	@Test
	@DisplayName("Slack error 메시지에 전달된 환경값이 그대로 포함된다")
	void errorAlertContainsProvidedEnvironment() {
		SlackMessage message = SlackMessage.createErrorAlert(
				"테스트 에러",
				"상세 메시지",
				"stacktrace",
				"/api/test",
				"user-1",
				"prod"
		);

		assertThat(message.getAttachments())
				.singleElement()
				.satisfies(attachment -> assertThat(attachment.getFields())
						.anySatisfy(field -> {
							assertThat(field.getTitle()).isEqualTo("환경");
							assertThat(field.getValue()).isEqualTo("prod");
						}));
	}
}
