package com.tnt.dto.trainer.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "트레이니와 연결 응답 - 트레이너의 화면")
public record ConnectWithTraineeResponse(
	@Schema(description = "트레이너 이름", example = "김철수", nullable = false)
	String trainerName,

	@Schema(description = "트레이니 이름", example = "홍길동", nullable = false)
	String traineeName,

	@Schema(description = "트레이너 프로필 이미지 URL", example = "https://images.tntapp.co.kr/a3hf2.jpg", nullable = false)
	String trainerProfileImageUrl,

	@Schema(description = "트레이니 프로필 이미지 URL", example = "https://images.tntapp.co.kr/3h4f.jpg", nullable = false)
	String traineeProfileImageUrl,

	@Schema(description = "트레이니 나이", example = "25", nullable = true)
	Integer traineeAge,

	@Schema(description = "트레이니 키", example = "178.6cm", nullable = false)
	Double height,

	@Schema(description = "트레이니 몸무게", example = "70.2kg", nullable = false)
	Double weight,

	@Schema(description = "PT 목표", example = "체중 감량, 근력 향상, 건강 관리", nullable = false)
	String ptGoal,

	@Schema(description = "주의 사항", example = "왼쪽 발목 골절", nullable = true)
	String cautionNote
) {

}
