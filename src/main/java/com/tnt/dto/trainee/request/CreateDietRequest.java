package com.tnt.dto.trainee.request;

import java.time.LocalDate;
import java.time.LocalTime;

import com.tnt.domain.trainee.DietType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

@Schema(description = "식단 등록 API 요청")
public record CreateDietRequest(
	@Schema(description = "식사 날짜", example = "2025-01-01", nullable = true)
	@PastOrPresent(message = "식사 날짜는 현재거나 과거 날짜여야 합니다.")
	LocalDate date,

	@Schema(description = "식사 시간", example = "19:30", nullable = true)
	@PastOrPresent(message = "식사 시간은 현재거나 과거 시간이어야 합니다.")
	LocalTime time,

	@Schema(description = "식단 타입", example = "BREAKFAST", nullable = false)
	@NotNull(message = "식단 타입은 필수입니다.")
	DietType dietType,

	@Schema(description = "메모", example = "아 배부르다.", nullable = false)
	@NotBlank(message = "메모는 필수입니다.")
	String memo
) {

}
