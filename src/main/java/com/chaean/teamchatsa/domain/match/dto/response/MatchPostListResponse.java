package com.chaean.teamchatsa.domain.match.dto.response;

import com.chaean.teamchatsa.domain.match.model.MatchPostStatus;
import com.chaean.teamchatsa.domain.team.model.TeamLevel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchPostListResponse {

	private Long postId;
	private String matchTitle;
	private String placeName;
	private LocalDate matchDate;
	private LocalTime matchTime;
	private String teamName;
	private String matchAddress;
	private MatchPostStatus postStatus;
	private Integer teamLevel;
	private String teamLevelLabel;
	private Integer headCount;

	public MatchPostListResponse(Long postId,
			String title,
			String placeName,
			LocalDateTime matchDateTime,
			String teamName,
			String address,
			MatchPostStatus postStatus,
			TeamLevel level,
			Integer headCount
	) {
		this.postId = postId;
		this.matchTitle = title;
		this.placeName = placeName;
		this.matchDate = matchDateTime.toLocalDate();
		this.matchTime = matchDateTime.toLocalTime();
		this.teamName = teamName;
		this.matchAddress = address;
		this.postStatus = postStatus;
		this.teamLevel = level != null ? level.getValue() : null;
		this.teamLevelLabel = level != null ? level.getDescription() : null;
		this.headCount = headCount;
	}
}
