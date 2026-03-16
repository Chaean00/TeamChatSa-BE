package com.chaean.teamchatsa.domain.match.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class MatchResultCreateRequest {
    @NotNull(message = "매치 포스트 ID는 필수입니다.")
    private Long matchPostId;

    @NotNull(message = "홈 팀 ID는 필수입니다.")
    private Long homeTeamId;

    @NotNull(message = "어웨이 팀 ID는 필수입니다.")
    private Long awayTeamId;

    @NotNull(message = "홈 팀 점수는 필수입니다.")
    private Integer homeScore;

    @NotNull(message = "어웨이 팀 점수는 필수입니다.")
    private Integer awayScore;
}
