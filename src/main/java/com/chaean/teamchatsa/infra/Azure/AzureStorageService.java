package com.chaean.teamchatsa.infra.Azure;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.chaean.teamchatsa.global.common.aop.annotation.Loggable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AzureStorageService {

	private final BlobServiceClient blobServiceClient;
	@Value("${azure.storage.container-name}")
	private String containerName;

	@Value("${azure.storage.sas-expire-seconds}")
	private long expireSeconds;

	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
			MediaType.IMAGE_PNG_VALUE,
			MediaType.IMAGE_JPEG_VALUE,
			"image/webp",
			"image/jpg"
	);

	@Loggable
	public PresignUploadRes presignUpload(PresignUploadReq req, Long userId) {
		validate(req);

		String blobName = buildBlobName(req.getFileName(), userId);

		BlobContainerClient containerClient =
				blobServiceClient.getBlobContainerClient(containerName);
		BlobClient blobClient = containerClient.getBlobClient(blobName);

		// 업로드용 SAS Token 생성
		BlobSasPermission permissions = new BlobSasPermission()
				.setWritePermission(true)
				.setCreatePermission(true);

		OffsetDateTime expiryTime = OffsetDateTime.now().plusSeconds(expireSeconds);

		BlobServiceSasSignatureValues sasValues = new
				BlobServiceSasSignatureValues(expiryTime, permissions)
				.setContentType(req.getContentType());

		// Azure SAS == Aws S3 presigned URL
		String sasToken = blobClient.generateSas(sasValues);
		String sasUrl = blobClient.getBlobUrl() + "?" + sasToken;

		log.info("SAS URL: {}", sasUrl);

		// 응답에 publicUrl 추가
		return new PresignUploadRes(blobName, sasUrl, expireSeconds);
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

	/** Blob 이름 생성 규칙: 사용자 prefix + 날짜 경로 + UUID + 확장자 */
	private String buildBlobName(String originalName, Long userId) {
		String ext = "";
		int dot = originalName.lastIndexOf('.');
		if (dot > -1) {
			ext = originalName.substring(dot).toLowerCase();
		}
		String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		return "uploads/u-" + userId + "/" + datePath + "/" + UUID.randomUUID() + ext;
	}
}


