package com.tnt.dto.trainer.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "트레이너 초대 코드 인증 응답")
public record InvitationCodeVerifyResponse(
	@Schema(description = "초대 코드 인증 여부", example = "true", nullable = false)
	boolean isVerified
) {

}
