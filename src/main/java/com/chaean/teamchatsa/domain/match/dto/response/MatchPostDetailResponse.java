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
public class MatchPostDetailResponse {

	private Long postId;
	private Long teamId;
	private String title;
	private String content;
	private String placeName;
	private String address;
	private double lat;
	private double lng;
	private LocalDate matchDate;
	private LocalTime matchTime;
	private String teamName;
	private String teamImg;
	private Integer teamLevel;
	private String teamLevelLabel;

	public MatchPostDetailResponse(Long postId,
			Long teamId,
			String title,
			String content,
			String placeName,
			String address,
			double lat,
			double lng,
			LocalDateTime matchDateTime,
			String teamName,
			String teamImg,
			TeamLevel teamLevel) {
		this.postId = postId;
		this.teamId = teamId;
		this.title = title;
		this.content = content;
		this.placeName = placeName;
		this.address = address;
		this.lat = lat;
		this.lng = lng;
		this.matchDate = matchDateTime.toLocalDate();
		this.matchTime = matchDateTime.toLocalTime();
		this.teamName = teamName;
		this.teamImg = teamImg;
		this.teamLevel = teamLevel != null ? teamLevel.getValue() : null;
		this.teamLevelLabel = teamLevel != null ? teamLevel.getDescription() : null;
	}
}
