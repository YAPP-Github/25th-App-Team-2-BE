package com.tnt.dto.trainee.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.tnt.domain.trainee.DietType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "트레이니 홈 기록 리스트 조회 응답")
public record GetTraineeDailyRecordsResponse(

	@Schema(description = "날짜", example = "2025-02-01", nullable = false)
	LocalDate date,

	@Schema(description = "PT 정보", nullable = true)
	PtInfo ptInfo,

	@Schema(description = "식단 목록", nullable = false)
	List<DietRecord> diets
) {

	public record PtInfo(
		@Schema(description = "트레이너 이름", example = "홍길동", nullable = false)
		String trainerName,

		@Schema(description = "PT 회차", example = "8", nullable = false)
		Integer session,

		@Schema(description = "PT 시작 시간", example = "2025-02-01T17:00:00", nullable = false)
		LocalDateTime lessonStart,

		@Schema(description = "PT 종료 시간", example = "2025-02-01T18:30:00", nullable = false)
		LocalDateTime lessonEnd
	) {

	}

	public record DietRecord(
		@Schema(description = "식단 ID", example = "54924", nullable = false)
		Long dietId,

		@Schema(description = "식사 날짜", example = "2025-01-01T11:00:00", nullable = false)
		LocalDateTime date,

		@Schema(description = "식단 사진", example = "https://images.tntapp.co.kr/a3hf2.jpg", nullable = true)
		String dietImageUrl,

		@Schema(description = "식단 타입", example = "BREAKFAST", nullable = false)
		DietType dietType,

		@Schema(description = "메모", example = "아 배부르다.", nullable = false)
		String memo
	) {

	}
}
