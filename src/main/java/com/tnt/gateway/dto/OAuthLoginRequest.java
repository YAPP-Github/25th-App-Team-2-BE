package com.tnt.gateway.dto;

import com.tnt.domain.member.SocialType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "소셜 로그인 API 요청")
public record OAuthLoginRequest(
	@Schema(description = "소셜 로그인 타입", example = "KAKAO", nullable = false)
	SocialType socialType,

	@Schema(description = "FCM 토큰", example = "dsl5f7iho-28yg2g290u2fj0-23348-23r05", nullable = false)
	@NotBlank(message = "FCM 토큰은 필수입니다.")
	String fcmToken,

	@Schema(description = "소셜 액세스 토큰 (카카오 로그인 시)", example = "atweroiuhoresihsgfkn", nullable = true)
	String socialAccessToken,

	@Schema(description = "인가 코드 (애플 Android 로그인 시)", example = "1231231231231", nullable = true)
	String authorizationCode,

	@Schema(description = "ID 토큰 (애플 iOS 로그인 시)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", nullable = true)
	String idToken
) {

}
