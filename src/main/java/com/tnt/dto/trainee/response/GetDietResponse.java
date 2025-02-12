package com.tnt.dto.trainee.response;

import java.time.LocalDateTime;

import com.tnt.domain.trainee.DietType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public record GetDietResponse(
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
