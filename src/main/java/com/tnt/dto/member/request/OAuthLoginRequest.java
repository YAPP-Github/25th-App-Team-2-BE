package com.tnt.dto.member.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "소셜 로그인 요청")
public record OAuthLoginRequest(
	@Schema(description = "소셜 로그인 타입", example = "kakao", type = "string", allowableValues = {"kakao", "apple"})
	@NotBlank(message = "소셜 로그인 타입은 필수입니다.")
	String socialType,

	@Schema(description = "소셜 액세스 토큰 (카카오 로그인 시)", example = "atweroiuhoresihsgfkn", type = "string")
	String socialAccessToken,

	@Schema(description = "인가 코드 (애플 Android 로그인 시)", example = "1231231231231", type = "string")
	String authorizationCode,

	@Schema(description = "ID 토큰 (애플 iOS 로그인 시)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", type = "string")
	String idToken
) {

}
