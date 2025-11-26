package com.chaean.teamchatsa.global.common.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;

/** 페이지 번호가 존재하지 않는 슬라이스 응답 DTO */

@Getter
@Builder
public class SliceResponse<T> {
	private final List<T> content;
	private final boolean last;

	public static <T> SliceResponse<T> from(Slice<T> slice) {
		return SliceResponse.<T>builder()
				.content(slice.getContent())
				.last(slice.isLast())
				.build();
	}
}
