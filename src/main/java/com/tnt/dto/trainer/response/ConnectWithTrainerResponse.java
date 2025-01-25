package com.tnt.dto.trainer.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "트레이너와 연결 응답 - 트레이니의 화면")
public record ConnectWithTrainerResponse(
	@Schema(description = "트레이너 이름", example = "김철수", nullable = false)
	String trainerName,

	@Schema(description = "트레이니 이름", example = "홍길동", nullable = false)
	String traineeName,

	@Schema(description = "트레이너 프로필 이미지 URL", example = "https://images.tntapp.co.kr/a3hf2.jpg", nullable = false)
	String trainerProfileImageUrl,

	@Schema(description = "트레이니 프로필 이미지 URL", example = "https://images.tntapp.co.kr/3h4f.jpg", nullable = false)
	String traineeProfileImageUrl
) {

}
