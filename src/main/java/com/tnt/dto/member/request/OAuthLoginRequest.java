package com.tnt.dto.member.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "소셜 로그인 요청")
public record OAuthLoginRequest(
	@Schema(description = "소셜 로그인 타입", example = "KAKAO, APPLE")
	@NotBlank(message = "소셜 로그인 타입은 필수입니다.")
	String socialType,

	@Schema(description = "소셜 액세스 토큰", example = "access_token_example")
	String socialAccessToken,

	@Schema(description = "인가 코드", example = "authorization_code_example")
	String authorizationCode,

	@Schema(description = "ID 토큰", example = "id_token_example")
	String idToken
) {

}
