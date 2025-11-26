package com.chaean.teamchatsa.infra.gcp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PresignUploadRes {
	private String key;          // 파일 경로
	private String uploadUrl;    // 업로드용 Signed URL (PUT 전용)
	private String publicUrl;    // 공개 접근 URL (업로드 후 브라우저 접근용)
	private long expireSeconds;  // uploadUrl 만료 시간
}
