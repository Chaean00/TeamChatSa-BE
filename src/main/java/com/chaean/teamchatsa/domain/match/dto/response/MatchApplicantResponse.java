package com.chaean.teamchatsa.domain.match.dto.response;

import com.chaean.teamchatsa.domain.match.model.MatchApplicationStatus;
import com.chaean.teamchatsa.domain.team.model.TeamLevel;
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
	private Integer teamLevel;
	private String teamLevelLabel;
	private String message;
	private LocalDateTime appliedAt;
	private MatchApplicationStatus status;

	public MatchApplicantResponse(
			Long applicantId,
			Long teamId,
			String teamName,
			String teamImg,
			TeamLevel teamLevel,
			String message,
			LocalDateTime appliedAt,
			MatchApplicationStatus status
	) {
		this.applicantId = applicantId;
		this.teamId = teamId;
		this.teamName = teamName;
		this.teamImg = teamImg;
		this.teamLevel = teamLevel != null ? teamLevel.getValue() : null;
		this.teamLevelLabel = teamLevel != null ? teamLevel.getDescription() : null;
		this.message = message;
		this.appliedAt = appliedAt;
		this.status = status;
	}
}
