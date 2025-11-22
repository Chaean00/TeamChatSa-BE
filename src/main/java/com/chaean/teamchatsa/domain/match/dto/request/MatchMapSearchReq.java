package com.chaean.teamchatsa.domain.match.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class MatchMapSearchReq {
	@NotNull(message = "남서쪽 위도는 필수입니다.")
	@DecimalMin(value = "-90.0", message = "위도는 -90도 이상이어야 합니다.")
	@DecimalMax(value = "90.0", message = "위도는 90도 이하여야 합니다.")
	private Double swLat;

	@NotNull(message = "남서쪽 경도는 필수입니다.")
	@DecimalMin(value = "-180.0", message = "경도는 -180도 이상이어야 합니다.")
	@DecimalMax(value = "180.0", message = "경도는 180도 이하여야 합니다.")
	private Double swLng;

	@NotNull(message = "북동쪽 위도는 필수입니다.")
	@DecimalMin(value = "-90.0", message = "위도는 -90도 이상이어야 합니다.")
	@DecimalMax(value = "90.0", message = "위도는 90도 이하여야 합니다.")
	private Double neLat;

	@NotNull(message = "북동쪽 경도는 필수입니다.")
	@DecimalMin(value = "-180.0", message = "경도는 -180도 이상이어야 합니다.")
	@DecimalMax(value = "180.0", message = "경도는 180도 이하여야 합니다.")
	private Double neLng;

	// 필터 (선택)
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate startDate;
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate endDate;
	private Integer headCount;
	private String region;
}
