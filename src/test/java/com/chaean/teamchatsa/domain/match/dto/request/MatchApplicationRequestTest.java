package com.chaean.teamchatsa.domain.match.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MatchApplicationRequestTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@DisplayName("JSON object payload can be deserialized for match apply requests")
	void deserializeJsonObject() throws Exception {
		String json = """
				{
				  "message": "신청합니다."
				}
				""";

		MatchApplicationRequest request = objectMapper.readValue(json, MatchApplicationRequest.class);

		assertThat(request.getMessage()).isEqualTo("신청합니다.");
	}
}
