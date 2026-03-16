package com.chaean.teamchatsa.infra.slack;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlackMessage {

	private String text;
	private List<Attachment> attachments;

	/** 에러 알림 메시지 생성 */
	public static SlackMessage createErrorAlert(
			String errorTitle,
			String errorMessage,
			String stackTrace,
			String endpoint,
			String userId
	) {
		LocalDateTime now = LocalDateTime.now();
		String timestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

		return SlackMessage.builder()
				.text("🚨 시스템 에러 발생")
				.attachments(List.of(
						Attachment.builder()
								.color("danger")
								.title("🚨 " + errorTitle)
								.text(errorMessage)
								.fields(List.of(
										Field.builder()
												.title("발생 시간")
												.value(timestamp)
												.shortField(true)
												.build(),
										Field.builder()
												.title("요청 URI")
												.value(endpoint != null ? endpoint : "N/A")
												.shortField(true)
												.build(),
										Field.builder()
												.title("사용자 ID")
												.value(userId != null ? userId : "Unknown")
												.shortField(true)
												.build(),
										Field.builder()
												.title("환경")
												.value(System.getProperty("spring.profiles.active", "dev"))
												.shortField(true)
												.build(),
										Field.builder()
												.title("Stack Trace")
												.value("```" + (stackTrace != null ?
														truncate(stackTrace, 500) : "N/A") + "```")
												.shortField(false)
												.build()
								))
								.footer("TeamChatSa Alert System")
								.ts(System.currentTimeMillis() / 1000)
								.build()
				))
				.build();
	}

	/** 비동기 작업 실패 알림 메시지 생성 */
	public static SlackMessage createAsyncFailureAlert(
			String methodName,
			String errorMessage,
			Object[] params
	) {
		return SlackMessage.builder()
				.text("⚠️ 비동기 작업 실패")
				.attachments(List.of(
						Attachment.builder()
								.color("warning")
								.title("⚠️ 비동기 메서드 실행 실패")
								.fields(List.of(
										Field.builder()
												.title("메서드명")
												.value(methodName)
												.shortField(true)
												.build(),
										Field.builder()
												.title("에러 메시지")
												.value(errorMessage)
												.shortField(false)
												.build(),
										Field.builder()
												.title("파라미터")
												.value(params != null ?
														String.join(", ",
																java.util.Arrays.toString(params)) : "N/A")
												.shortField(false)
												.build()
								))
								.footer("Async Task Monitor")
								.ts(System.currentTimeMillis() / 1000)
								.build()
				))
				.build();
	}

	/** 긴 텍스트 파싱 */
	private static String truncate(String text, int maxLength) {
		if (text == null || text.length() <= maxLength) {
			return text;
		}
		return text.substring(0, maxLength) + "... (truncated)";
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Attachment {

		private String color;
		private String pretext;
		private String title;
		private String text;
		private List<Field> fields;
		private String footer;
		private Long ts;
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Field {

		private String title;
		private String value;
		@Builder.Default
		private Boolean shortField = false;
	}
}
