package com.chaean.teamchatsa.domain.match.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
@Builder
public class MatchPostDetailRes {
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
	private String teamLevel;

	public MatchPostDetailRes(Long postId,
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
							  String teamLevel) {
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
		this.teamLevel = teamLevel;
	}
}
