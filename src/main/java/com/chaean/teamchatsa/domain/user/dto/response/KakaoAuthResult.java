package com.chaean.teamchatsa.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoAuthResult {
	private TokenResponse tokens;
	private boolean newUser;
}
