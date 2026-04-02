package com.chaean.teamchatsa;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"SPRING_PROFILES_ACTIVE=dev",
		"TZ=Asia/Seoul",
		"POSTGRES_HOST=localhost",
		"POSTGRES_PORT=5432",
		"POSTGRES_DB=tcs_postgre",
		"POSTGRES_USER=tcsuser",
		"POSTGRES_PASSWORD=1234",
		"REDIS_HOST=localhost",
		"REDIS_PORT=6379",
		"JWT_SECRET=test-jwt-secret-for-context-loads-1234567890",
		"KAKAO_SECRET_KEY=test-kakao-client-id",
		"KAKAO_CLIENT_SECRET_KEY=test-kakao-client-secret",
		"OPENAI_API_KEY=test-openai-key",
		"SLACK_WEBHOOK_URL=",
		"SLACK_ALERT_ENABLED=false",
		"APP_AUTH_REDIRECT_SUCCESS=http://localhost:3000/auth/kakao/callback",
		"APP_AUTH_REDIRECT_FAILURE=http://localhost:3000/auth/kakao/failure",
		"APP_UPLOAD_BASE_DIR=./uploads-test",
})
@ActiveProfiles("dev")
class TeamChatSaApplicationTests {

    @Test
    void contextLoads() {
    }

}
