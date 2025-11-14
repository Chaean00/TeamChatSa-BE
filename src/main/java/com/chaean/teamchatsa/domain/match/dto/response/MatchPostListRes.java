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
public class MatchPostListRes {
	private Long postId;
	private String matchTitle;
	private String placeName;
	private LocalDate matchDate;
	private LocalTime matchTime;
	private String teamName;
	private String matchAddress;
	private MatchPostStatus postStatus;
	private String teamLevel;

	public MatchPostListRes(Long postId,
							String title,
							String placeName,
							LocalDateTime matchDateTime,
							String teamName,
							String address,
							MatchPostStatus postStatus,
							String level) {
		this.postId = postId;
		this.matchTitle = title;
		this.placeName = placeName;
		this.matchDate = matchDateTime.toLocalDate();
		this.matchTime = matchDateTime.toLocalTime();
		this.teamName = teamName;
		this.matchAddress = address;
		this.postStatus = postStatus;
		this.teamLevel = level;
	}
}
