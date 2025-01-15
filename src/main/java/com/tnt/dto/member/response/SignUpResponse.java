package com.tnt.dto.member.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 API 응답")
public record SignUpResponse(
	@Schema(description = "회원 이름", example = "홍길동")
	String name,

	@Schema(description = "프로필 사진 URL", example = "asdf.png")
	String profileImageUrl,

	@Schema(description = "회원 타입 (trainer / trainee)", example = "trainer")
	String memberType
) {

}
