package com.chaean.teamchatsa.domain.match.dto.response;

import com.chaean.teamchatsa.domain.match.model.MatchApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MatchApplicantRes {
	private Long applicantId;
	private Long teamId;
	private String teamName;
	private String teamImg;
	private String teamLevel;
	private String message;
	private LocalDateTime appliedAt;
	private MatchApplicationStatus status;
}
