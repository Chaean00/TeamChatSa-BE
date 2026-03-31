package com.chaean.teamchatsa.infra.storage;

import com.chaean.teamchatsa.global.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "파일 업로드 API", description = "로컬 파일 업로드 API")
@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
public class UploadController {

	private final LocalStorageService localStorageService;

	@Operation(summary = "팀 이미지 업로드 API", description = "팀 생성에 사용할 이미지를 홈서버에 업로드합니다.")
	@PostMapping("/images")
	public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadTeamImage(
			@RequestParam("file") MultipartFile file,
			@AuthenticationPrincipal Long userId
	) {
		return ResponseEntity.ok(ApiResponse.success(localStorageService.uploadTeamImage(file, userId)));
	}
}
