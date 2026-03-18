package com.chaean.teamchatsa.domain.match.dto.response;

import com.chaean.teamchatsa.domain.team.model.TeamLevel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MatchMapResponse {

	private Long postId;
	private String matchTitle;
	private LocalDate matchDate;
	private LocalTime matchTime;
	private String teamName;
	private Integer teamLevel;
	private String teamLevelLabel;
	private Double lat;
	private Double lng;

	public MatchMapResponse(Long postId,
			String title,
			LocalDateTime matchDateTime,
			String teamName,
			Integer teamLevel,
			Double lat,
			Double lng
	) {
		this.postId = postId;
		this.matchTitle = title;
		this.matchDate = matchDateTime.toLocalDate();
		this.matchTime = matchDateTime.toLocalTime();
		this.teamName = teamName;
		this.teamLevel = teamLevel;
		this.teamLevelLabel = teamLevel != null ? TeamLevel.fromValue(teamLevel).getDescription() : null;
		this.lat = lat;
		this.lng = lng;
	}
}
