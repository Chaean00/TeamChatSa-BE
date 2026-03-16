package com.chaean.teamchatsa.global.common.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

/**
 * 페이지 번호가 존재하지 않는 슬라이스 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SliceResponse<T> {

	private List<T> content;
	private boolean last;

	public static <T> SliceResponse<T> from(Slice<T> slice) {
		return SliceResponse.<T>builder()
				.content(slice.getContent())
				.last(slice.isLast())
				.build();
	}
}
