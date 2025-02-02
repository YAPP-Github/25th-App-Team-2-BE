package com.tnt.gateway.dto.response;

import com.tnt.domain.member.MemberType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "세션 확인 API 응답")
public record CheckSessionResponse(
	@Schema(description = "회원 타입", example = "TRAINER", nullable = false)
	MemberType memberType
) {

}
