package com.chaean.teamchatsa.domain.match.dto.request;

import com.chaean.teamchatsa.domain.match.model.MatchPost;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record MatchPostCreateReq(
		@NotNull
		@Size(min = 1, max = 100)
		String title,
		@NotNull
		String content,
		@NotNull
		@Future
		LocalDateTime matchDate,
		@NotNull
		@Min(-90)
		@Max(90)
		double lat,
		@NotNull
		@Min(-180)
		@Max(180)
		double lng,
		@NotNull
		@Size(max = 255)
		String address,
		@Size(max = 120)
		String placeName
) {
	public MatchPost toEntity(Long teamId) {
		return MatchPost.builder()
				.teamId(teamId)
				.title(this.title)
				.content(this.content)
				.address(this.address)
				.matchDate(this.matchDate)
				.lat(this.lat)
				.lng(this.lng)
				.placeName(this.placeName)
				.build();
	}
}
