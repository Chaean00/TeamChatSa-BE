package com.chaean.teamchatsa.infra.gcp;

import com.chaean.teamchatsa.global.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "GCP Cloud Storage API", description = "GCP Cloud Storage 파일 업로드 API")
@RestController
@RequestMapping("/api/v1/gcp/storage")
@RequiredArgsConstructor
public class GcpStorageController {

	private final GcpStorageService storageService;

	@Operation(summary = "업로드용 Signed URL 발급", description = "파일 업로드를 위한 GCP Signed URL을 반환합니다.")
	@PostMapping("/presign-upload")
	public ResponseEntity<ApiResponse<PresignUploadRes>> presignUpload(
		@RequestBody PresignUploadReq req,
		@AuthenticationPrincipal Long userId
	) {
		PresignUploadRes res = storageService.presignUpload(req, userId);
		return ResponseEntity.ok(ApiResponse.success(res));
	}
}
