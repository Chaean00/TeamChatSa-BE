package com.chaean.teamchatsa.domain.match.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MatchPostCreateReq {
	@NotNull
	@Size(min = 1, max = 100)
	private String title;
	@NotNull
	private String content;
	@NotNull
	@Min(4) @Max(11)
	private int headCount;
	@NotNull
	@Future
	private LocalDateTime matchDate;
	@NotNull
	@Min(-90)
	@Max(90)
	private double lat;
	@NotNull
	@Min(-180)
	@Max(180)
	private double lng;
	@NotNull
	@Size(max = 255)
	private String address;
	@Size(max = 120)
	private String placeName;
}
