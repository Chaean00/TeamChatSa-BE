package com.chaean.teamchatsa.domain.match.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyMatchHistoryResponse {

	private Long matchPostId;
	private String matchTitle;
	private LocalDateTime matchDate;
	private String placeName;
	private String address;
	private String matchPhase;
	private Long homeTeamId;
	private String homeTeamName;
	private Long awayTeamId;
	private String awayTeamName;
	private Long opponentTeamId;
	private String opponentTeamName;
	private boolean resultRegistered;
	private Integer homeScore;
	private Integer awayScore;
	private boolean reviewWritten;
	private boolean canRegisterResult;
	private boolean canReview;
}
