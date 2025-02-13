package com.tnt.dto.trainee.response;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "트레이니 캘린더 PT 수업, 기록 있는 날짜 조회 응답")
public record GetTraineeCalendarPtLessonCountResponse(
	@Schema(description = "PT 수업, 기록 있는 날짜 목록", examples = {"2025-01-01", "2025-01-13"}, nullable = true)
	List<LocalDate> ptLessonDates
) {

}
