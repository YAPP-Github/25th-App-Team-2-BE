package com.tnt.dto.member.response;

import com.tnt.domain.member.MemberType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 API 응답")
public record SignUpResponse(
	@Schema(description = "회원 타입", example = "TRAINER", nullable = false)
	MemberType memberType,

	@Schema(description = "세션 ID", example = "1645365389", nullable = false)
	String sessionId,

	@Schema(description = "회원 이름", example = "홍길동", nullable = false)
	String name,

	@Schema(description = "프로필 사진 URL", example = "https://images.tntapp.co.kr/profiles/trainers/basic_profile_trainer.svg", nullable = false)
	String profileImageUrl
) {

}
