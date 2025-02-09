package com.tnt.dto.trainer.request;

import java.time.LocalDateTime;

import org.hibernate.validator.constraints.Length;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "PT 수업 생성 요청")
public record CreatePtLessonRequest(
	@Schema(description = "수업 시작 날짜 및 시간", example = "2025-03-20T10:00:00", nullable = false)
	LocalDateTime start,

	@Schema(description = "수업 끝 날짜 및 시간", example = "2025-03-20T11:00:00", nullable = false)
	LocalDateTime end,

	@Schema(description = "메모", example = "하체 운동 시키기", nullable = false)
	@Length(max = 30)
	String memo,

	@Schema(description = "트레이니 id", example = "213912408127", nullable = false)
	Long traineeId
) {

}
