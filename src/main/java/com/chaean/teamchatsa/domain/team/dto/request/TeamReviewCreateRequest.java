package com.chaean.teamchatsa.domain.team.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class TeamReviewCreateRequest {
    @NotNull(message = "대상 팀 ID는 필수입니다.")
    private Long teamId;

    @NotNull(message = "매칭 ID는 필수입니다.")
    private Long matchId;

    @Min(value = 1, message = "평점은 최소 1점입니다.")
    @Max(value = 5, message = "평점은 최대 5점입니다.")
    private int rating;

    @NotBlank(message = "리뷰 내용은 필수입니다.")
    private String content;
}
