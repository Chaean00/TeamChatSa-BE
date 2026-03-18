package com.chaean.teamchatsa.domain.match.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class MatchRecommendationRequest {

	@NotBlank(message = "검색어는 필수입니다.")
	@Size(max = 200, message = "검색어는 200자 이하로 입력해주세요.")
	private String query;
}
