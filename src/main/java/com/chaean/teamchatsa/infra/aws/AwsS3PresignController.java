package com.chaean.teamchatsa.infra.aws;

import com.chaean.teamchatsa.global.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/aws/s3")
@RequiredArgsConstructor
public class AwsS3PresignController {

	private final AwsS3Service s3Service;

	@PostMapping("/presign-upload")
	public ResponseEntity<ApiResponse<PresignUploadRes>> presignUpload(
			@RequestBody PresignUploadReq req,
			@AuthenticationPrincipal Long userId
	) {
		PresignUploadRes res = s3Service.presignUpload(req, userId);
		return ResponseEntity.ok(ApiResponse.success(res));
	}

	@GetMapping("/presign-download")
	public ResponseEntity<ApiResponse<PresignDownloadRes>> presignDownload(@RequestParam String key) {
		PresignDownloadRes res = s3Service.presignDownload(key);
		return ResponseEntity.ok(ApiResponse.success(res));
	}
}
