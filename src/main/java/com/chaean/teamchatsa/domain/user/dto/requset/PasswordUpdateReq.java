package com.chaean.teamchatsa.domain.user.dto.requset;

public record PasswordUpdateReq (
		String currentPassword,
		String newPassword
) {}
