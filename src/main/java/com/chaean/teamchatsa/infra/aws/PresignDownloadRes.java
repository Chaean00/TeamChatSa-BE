package com.chaean.teamchatsa.infra.aws;

public record PresignDownloadRes(
		String url,
		long expireSeconds
) {
}
