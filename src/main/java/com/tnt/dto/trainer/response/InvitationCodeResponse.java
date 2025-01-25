package com.tnt.dto.trainer.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "트레이너의 초대 코드 응답")
public record InvitationCodeResponse(
	@Schema(description = "트레이너의 초대 코드 (재발급 포함)", example = "2H9DG4X3", nullable = false)
	String invitationCode
) {

}
