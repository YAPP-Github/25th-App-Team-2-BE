package com.tnt.dto.trainer.response;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "트레이너 - 달력 스케쥴 개수 표시 응답")
public record GetCalendarPtLessonCountResponse(
	@Schema(description = "해당 월의 각 날짜의 PT 수업 개수", nullable = true)
	List<CalendarPtLessonCount> calendarPtLessonCounts
) {

	public record CalendarPtLessonCount(
		@Schema(description = "해당 날짜", example = "2025-01-29", nullable = false)
		LocalDate date,

		@Schema(description = "해당 날짜의 총 PT 수업 개수", example = "5", nullable = false)
		Integer count
	) {

	}
}
