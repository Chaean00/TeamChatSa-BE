package com.chaean.teamchatsa.domain.feedback.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.chaean.teamchatsa.domain.feedback.dto.request.FeedbackCreateRequest;
import com.chaean.teamchatsa.domain.feedback.repository.FeedbackRepository;
import com.chaean.teamchatsa.domain.user.model.User;
import com.chaean.teamchatsa.domain.user.repository.UserRepository;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.infra.slack.SlackAlertService;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

	@Mock
	private FeedbackRepository feedbackRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private SlackAlertService slackAlertService;

	@InjectMocks
	private FeedbackService feedbackService;

	@Nested
	@DisplayName("의견 저장")
	class CreateFeedback {

		@Test
		@DisplayName("성공 - 닉네임이 있으면 닉네임으로 저장")
		void success_withNickname() {
			User user = User.builder()
					.id(1L)
					.username("tester")
					.nickname("테스터")
					.password("encoded")
					.build();
			FeedbackCreateRequest request = new FeedbackCreateRequest("  의견 남깁니다.  ");

			given(userRepository.findById(1L)).willReturn(Optional.of(user));

			feedbackService.createFeedback(1L, request);

			verify(feedbackRepository).save(argThat(feedback ->
					feedback.getSenderUserId().equals(1L)
							&& feedback.getSenderName().equals("테스터")
							&& feedback.getContent().equals("의견 남깁니다.")
			));
			verify(slackAlertService).sendFeedbackAlert(1L, "테스터", "의견 남깁니다.");
		}

		@Test
		@DisplayName("실패 - 유저가 없으면 예외")
		void fail_userNotFound() {
			given(userRepository.findById(99L)).willReturn(Optional.empty());

			assertThatThrownBy(() -> feedbackService.createFeedback(99L, new FeedbackCreateRequest("의견")))
					.isInstanceOf(BusinessException.class)
					.hasMessageContaining("유저 정보를 찾을 수 없습니다.");
		}
	}
}
