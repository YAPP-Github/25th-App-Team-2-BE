package com.tnt.dto.member.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 탈퇴 API 요청")
public record WithdrawRequest(
	@Schema(description = "소셜 액세스 토큰 (카카오 로그인 시)", example = "atweroiuhoresihsgfkn", nullable = true)
	String socialAccessToken,

	@Schema(description = "인가 코드 (애플 로그인 시)", example = "1231231231231", nullable = true)
	String authorizationCode
) {

}
