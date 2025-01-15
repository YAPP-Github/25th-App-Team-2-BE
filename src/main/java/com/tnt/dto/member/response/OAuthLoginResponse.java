package com.tnt.dto.member.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "소셜 로그인 API 응답")
public record OAuthLoginResponse(
	@Schema(description = "세션 ID (null / 1645365389)", example = "1645365389")
	String sessionId,

	@Schema(description = "소셜 ID (null / 43252465)", example = "43252465")
	String socialId,

	@Schema(description = "소셜 이메일 (null / zxc098@kakao.com)", example = "zxc098@kakao.com")
	String socialEmail,

	@Schema(description = "소셜 로그인 타입 (null / KAKAO)", example = "KAKAO")
	String socialType,

	@Schema(description = "가입 여부 (true / false)", example = "false")
	boolean isSignUp
) {

}
