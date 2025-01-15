package com.tnt.dto.trainer.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "트레이너의 초대 코드 응답")
public record InvitationCodeResponse(

	@Schema(description = "트레이너 id", example = "23984725", type = "string")
	String trainerId,

	@Schema(description = "트레이너의 초대 코드", example = "3h38g9h3", type = "string")
	String invitationCode
) {

}
