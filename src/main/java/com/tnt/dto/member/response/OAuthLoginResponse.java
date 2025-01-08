package com.tnt.dto.member.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "소셜 로그인 응답")
@Builder
public record OAuthLoginResponse(
	@Schema(description = "세션 ID", example = "123456789")
	String sessionId
) {

	public static OAuthLoginResponse from(String sessionId) {
		return new OAuthLoginResponse(sessionId);
	}
}
