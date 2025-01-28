package com.tnt.gateway.dto;

import com.tnt.domain.member.MemberType;
import com.tnt.domain.member.SocialType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "소셜 로그인 API 응답")
public record OAuthLoginResponse(
	@Schema(description = "세션 ID", example = "1645365389", nullable = true)
	String sessionId,

	@Schema(description = "소셜 ID", example = "43252465", nullable = true)
	String socialId,

	@Schema(description = "소셜 이메일", example = "zxc098@kakao.com", nullable = true)
	String socialEmail,

	@Schema(description = "소셜 로그인 타입", example = "KAKAO", nullable = true)
	SocialType socialType,

	@Schema(description = "가입 여부", example = "false", nullable = false)
	Boolean isSignUp,

	@Schema(description = "회원 타입", example = "TRAINEE", nullable = false)
	MemberType memberType
) {

}
