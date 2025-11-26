package com.chaean.teamchatsa.domain.match.dto.response;

import com.chaean.teamchatsa.domain.match.model.MatchPostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
@Builder
public class MatchMapRes {

	private Long postId;
	private String matchTitle;
//	private String placeName;
	private LocalDate matchDate;
	private LocalTime matchTime;
	private String teamName;
	private String teamLevel;
//	private String matchAddress;
//	private MatchPostStatus postStatus;
	private Double lat;
	private Double lng;

	public MatchMapRes(Long postId,
					   String title,
					   LocalDateTime matchDateTime,
					   String teamName,
					   String teamLevel,
					   Double lat,
					   Double lng) {
		this.postId = postId;
		this.matchTitle = title;
//		this.placeName = placeName;
		this.matchDate = matchDateTime.toLocalDate();
		this.matchTime = matchDateTime.toLocalTime();
		this.teamName = teamName;
//		this.matchAddress = address;
//		this.postStatus = postStatus;
		this.teamLevel = teamLevel;
		this.lat = lat;
		this.lng = lng;
	}
}
