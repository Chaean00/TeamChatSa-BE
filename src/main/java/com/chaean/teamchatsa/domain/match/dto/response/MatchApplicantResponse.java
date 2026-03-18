package com.chaean.teamchatsa.domain.match.dto.response;

import com.chaean.teamchatsa.domain.match.model.MatchApplicationStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MatchApplicantResponse {

	private Long applicantId;
	private Long teamId;
	private String teamName;
	private String teamImg;
	private String teamLevel;
	private String message;
	private LocalDateTime appliedAt;
	private MatchApplicationStatus status;
}
