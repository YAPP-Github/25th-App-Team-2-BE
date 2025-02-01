package com.tnt.dto.trainer.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Schema(description = "트레이너 연결 요청")
public record ConnectWithTrainerRequest(
	@Schema(description = "트레이너의 초대 코드", example = "2H9DG4X3", nullable = false)
	@Size(min = 8, max = 8, message = "초대 코드는 8자리여야 합니다.")
	String invitationCode,

	@Schema(description = "PT 시작일", example = "2025-03-20", nullable = false)
	LocalDate startDate,

	@Schema(description = "총 등록 회차", example = "5", nullable = false)
	@Positive(message = "총 등록 회차는 1 이상이어야 합니다.")
	Integer totalPtCount,

	@Schema(description = "현재 완료 회차", example = "3", nullable = false)
	@PositiveOrZero(message = "현재 완료 회차는 0 이상이어야 합니다.")
	Integer finishedPtCount
) {

}
