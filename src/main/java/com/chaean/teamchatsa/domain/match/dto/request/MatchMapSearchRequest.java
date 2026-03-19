package com.chaean.teamchatsa.domain.match.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@ToString
public class MatchMapSearchRequest {

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

	@NotNull(message = "줌 레벨은 필수입니다.")
	@Min(value = 1, message = "줌 레벨은 1 이상이어야 합니다.")
	@Max(value = 14, message = "줌 레벨은 14 이하여야 합니다.")
	private Integer zoomLevel;

	// 필터 (선택)
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate startDate;
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate endDate;
	private Integer headCount;
}
