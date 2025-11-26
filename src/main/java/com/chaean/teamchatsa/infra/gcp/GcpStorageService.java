package com.chaean.teamchatsa.infra.gcp;

import com.chaean.teamchatsa.global.common.aop.annotation.Loggable;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class GcpStorageService {

	// Spring Cloud GCP가 자동으로 주입 (spring.cloud.gcp 설정 기반)
	private final Storage storage;

	@Value("${gcp.storage.bucket-name}")
	private String bucketName;

	@Value("${gcp.storage.signed-url-expire-minutes}")
	private long expireMinutes;

	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
		MediaType.IMAGE_PNG_VALUE,
		MediaType.IMAGE_JPEG_VALUE,
		"image/webp",
		"image/jpg"
	);

	@Loggable
	public PresignUploadRes presignUpload(PresignUploadReq req, Long userId) {
		validate(req);

		String objectName = buildObjectName(req.getFileName(), userId);

		BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, objectName))
			.setContentType(req.getContentType())
			.build();

		// Signed URL 생성 (업로드용)
		URL signedUrl = storage.signUrl(
			blobInfo,
			expireMinutes,
			TimeUnit.MINUTES,
			Storage.SignUrlOption.withV4Signature(),
			Storage.SignUrlOption.httpMethod(com.google.cloud.storage.HttpMethod.PUT)
		);

		// 공개 접근 URL 생성
		String publicUrl = String.format("https://storage.googleapis.com/%s/%s", bucketName, objectName);

		log.info("GCP Public URL: {}", publicUrl);

		long expireSeconds = expireMinutes * 60;
		return new PresignUploadRes(objectName, signedUrl.toString(), publicUrl, expireSeconds);
	}

	/** 요청 유효성 검증 */
	private void validate(PresignUploadReq req) {
		if (req.getFileName() == null || req.getFileName().isBlank()) {
			throw new IllegalArgumentException("파일 이름(fileName)은 필수 값입니다.");
		}
		if (req.getContentType() == null || !ALLOWED_CONTENT_TYPES.contains(req.getContentType())) {
			throw new IllegalArgumentException("지원하지 않는 파일 형식입니다: " + req.getContentType());
		}
	}

	/** Object 이름 생성 규칙: 사용자 prefix + 날짜 경로 + UUID + 확장자 */
	private String buildObjectName(String originalName, Long userId) {
		String ext = "";
		int dot = originalName.lastIndexOf('.');
		if (dot > -1) {
			ext = originalName.substring(dot).toLowerCase();
		}
		String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		return "uploads/u-" + userId + "/" + datePath + "/" + UUID.randomUUID() + ext;
	}
}
