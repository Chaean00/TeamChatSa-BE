package com.chaean.teamchatsa.infra.storage;

import com.chaean.teamchatsa.global.common.aop.annotation.Loggable;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class LocalStorageService {

	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
			MediaType.IMAGE_PNG_VALUE,
			MediaType.IMAGE_JPEG_VALUE,
			"image/webp",
			"image/jpg"
	);

	private final Path basePath;

	public LocalStorageService(@Value("${app.upload.base-dir:./uploads}") String baseDir) {
		this.basePath = Paths.get(baseDir).toAbsolutePath().normalize();
	}

	@Loggable
	public ImageUploadResponse uploadTeamImage(MultipartFile file, Long userId) {
		validate(file);

		try {
			LocalDate today = LocalDate.now();
			String extension = extractExtension(file.getOriginalFilename());
			Path relativePath = Paths.get(
					"teams",
					"u-" + userId,
					String.valueOf(today.getYear()),
					String.format("%02d", today.getMonthValue()),
					String.format("%02d", today.getDayOfMonth()),
					UUID.randomUUID() + extension
			);
			Path targetPath = basePath.resolve(relativePath).normalize();

			if (!targetPath.startsWith(basePath)) {
				throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "잘못된 업로드 경로입니다.");
			}

			Files.createDirectories(targetPath.getParent());
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
			}

			String publicUrl = "/api/v1/uploads/files/" + relativePath.toString().replace('\\', '/');
			log.info("Local file uploaded: {}", publicUrl);
			return new ImageUploadResponse(publicUrl);
		} catch (IOException e) {
			throw new BusinessException(ErrorCode.INTERNAL_ERROR, "이미지 업로드에 실패했습니다.");
		}
	}

	private void validate(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "업로드할 파일이 없습니다.");
		}

		String contentType = file.getContentType();
		if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "지원하지 않는 파일 형식입니다.");
		}
	}

	private String extractExtension(String originalFilename) {
		if (originalFilename == null) {
			return "";
		}

		int dotIndex = originalFilename.lastIndexOf('.');
		if (dotIndex < 0) {
			return "";
		}

		return originalFilename.substring(dotIndex).toLowerCase();
	}
}
