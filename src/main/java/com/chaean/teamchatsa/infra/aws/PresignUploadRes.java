package com.chaean.teamchatsa.infra.aws;

public record PresignUploadRes(
		String key,
		String url,
		long expireSeconds
) {
}
