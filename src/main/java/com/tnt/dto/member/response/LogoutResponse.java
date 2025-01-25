package com.tnt.dto.member.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그아웃 API 응답")
public record LogoutResponse(
	@Schema(description = "세션 ID", example = "1645365389", nullable = false)
	String sessionId
) {

}
