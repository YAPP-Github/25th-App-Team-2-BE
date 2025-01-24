package com.tnt.dto.member.request;

import static com.tnt.domain.constant.Constant.APPLE;
import static com.tnt.domain.constant.Constant.KAKAO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "소셜 로그인 API 요청")
public record OAuthLoginRequest(
	@Schema(description = "소셜 로그인 타입 (KAKAO / APPLE)", example = "KAKAO", allowableValues = {"KAKAO",
		"APPLE"}, nullable = false)
	@Pattern(regexp = "^(KAKAO|APPLE)$", message = "소셜 로그인 타입은 KAKAO 또는 APPLE만 가능합니다.")
	String socialType,

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

	@AssertTrue(message = "카카오 로그인 시 소셜 액세스 토큰은 필수입니다.")
	public boolean validateKakaoLogin() {
		if (KAKAO.equals(socialType)) {
			return socialAccessToken != null && !socialAccessToken.isBlank();
		}

		return true;
	}

	@AssertTrue(message = "애플 로그인 시 인가 코드 또는 ID 토큰 중 하나는 필수입니다.")
	public boolean validateAppleLogin() {
		if (APPLE.equals(socialType)) {
			return (authorizationCode != null && !authorizationCode.isBlank()) || (idToken != null
				&& !idToken.isBlank());
		}

		return true;
	}
}
