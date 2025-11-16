package com.chaean.teamchatsa.infra.aws;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PresignUploadRes {
	private String key;
	private String url;
	private long expireSeconds;
}
