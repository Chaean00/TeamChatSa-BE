package com.chaean.teamchatsa.domain.match.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MatchPostCreateRequest {

	@NotNull
	@Size(min = 1, max = 100)
	private String title;
	@NotNull
	private String content;
	@NotNull
	@Min(4)
	@Max(11)
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
