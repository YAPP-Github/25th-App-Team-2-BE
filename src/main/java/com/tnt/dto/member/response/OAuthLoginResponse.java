package com.tnt.dto.member.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "소셜 로그인 응답")
@Builder
public record OAuthLoginResponse(
	@Schema(description = "세션 ID (null / 1236543)", example = "1645365389", type = "string")
	String sessionId,

	@Schema(description = "소셜 ID (null / 1236543)", example = "43252465", type = "string")
	String socialId,

	@Schema(description = "가입 여부 (true / false)", example = "false", type = "boolean")
	boolean isSignUp
) {

	public static OAuthLoginResponse from(String sessionId, String socialId, boolean isSignUp) {
		return new OAuthLoginResponse(sessionId, socialId, isSignUp);
	}
}
