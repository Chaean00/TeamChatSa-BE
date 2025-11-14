package com.chaean.teamchatsa.infra.aws;

import com.chaean.teamchatsa.global.common.aop.annotation.Loggable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AwsS3Service {

	private final S3Presigner s3Presigner;
	@Value("${aws.s3.bucket-name}")
	private String bucketName;
	@Value("${aws.s3.url-expire-seconds}")
	private long expireSeconds;

	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
			MediaType.IMAGE_PNG_VALUE,
			MediaType.IMAGE_JPEG_VALUE,
			"image/webp",
			"image/jpg"
	);

	/** 업로드용 Presigned URL 발급 */
	@Loggable
	public PresignUploadRes presignUpload(PresignUploadReq req, Long userId) {
		validate(req);

		String key = buildObjectKey(req.getFileName(), userId);

		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(key)
				.contentType(req.getContentType()) // 서명에 포함되므로 클라이언트 PUT과 동일해야 함
				.build();

		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
				.signatureDuration(Duration.ofSeconds(expireSeconds))
				.putObjectRequest(putObjectRequest)
				.build();

		PresignedPutObjectRequest presignedPutObjectRequest = s3Presigner.presignPutObject(presignRequest);
		URL url = presignedPutObjectRequest.url();

		log.info("Presigned URL 발급 완료. key={}, 만료시간={}초", key, expireSeconds);
		return new PresignUploadRes(key, url.toString(), expireSeconds);
	}

	private void validate(PresignUploadReq req) {
		if (req.getFileName() == null || req.getFileName().isBlank()) {
			throw new IllegalArgumentException("파일 이름(fileName)은 필수 값입니다.");
		}
		if (req.getContentType() == null || !ALLOWED_CONTENT_TYPES.contains(req.getContentType())) {
			throw new IllegalArgumentException("지원하지 않는 파일 형식입니다: " + req.getContentType());
		}
	}

	/** Key 규칙: 사용자 prefix + 날짜 경로 + UUID + 확장자 */
	private String buildObjectKey(String originalName, Long userId) {
		String ext = "";
		int dot = originalName.lastIndexOf('.');
		if (dot > -1) {
			ext = originalName.substring(dot).toLowerCase();
		}
		String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		return "uploads/u-" + userId + "/" + datePath + "/" + UUID.randomUUID() + ext;
	}
}
