package com.tnt.dto.trainer.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "트레이너 - 특정 날짜의 수업 리스트 요청 응답")
public record GetPtLessonsOnDateResponse(
	@Schema(description = "해당 날짜의 총 수업 개수", example = "5", nullable = false)
	Integer count,

	@Schema(description = "해당 날짜", example = "2025-01-29", nullable = false)
	LocalDate date,

	@Schema(description = "각 수업에 대한 정보", nullable = true)
	List<Lesson> lessons
) {

	public record Lesson(
		@Schema(description = "PT 수업 ID", example = "49871314", nullable = false)
		String ptLessonId,

		@Schema(description = "트레이니 ID", example = "123523564", nullable = false)
		String traineeId,

		@Schema(description = "트레이니 이름", example = "김정호", nullable = false)
		String traineeName,

		@Schema(description = "PT 회차", example = "6", nullable = false)
		Integer session,

		@Schema(description = "PT 시작 시간", example = "2025-01-29T18:00:00", nullable = false)
		LocalDateTime startTime,

		@Schema(description = "PT 종료 시간", example = "2025-01-29T19:00:00", nullable = false)
		LocalDateTime endTime,

		@Schema(description = "수업 완료 여부", example = "true", nullable = false)
		Boolean isCompleted
	) {

	}
}
