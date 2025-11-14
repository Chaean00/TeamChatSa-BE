package com.chaean.teamchatsa.infra.aws;

public record PresignUploadReq(
		String fileName,
		String contentType
) {
}
