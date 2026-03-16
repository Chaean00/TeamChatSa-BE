package com.chaean.teamchatsa.infra.gcp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PresignUploadRequest {

	private String fileName;
	private String contentType;
}
