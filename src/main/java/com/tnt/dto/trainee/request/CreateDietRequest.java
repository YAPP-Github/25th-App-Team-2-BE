package com.tnt.dto.trainee.request;

import java.time.LocalDateTime;

import com.tnt.domain.trainee.DietType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

@Schema(description = "식단 등록 API 요청")
public record CreateDietRequest(
	@Schema(description = "식사 날짜", example = "2025-01-01T11:00:00", nullable = true)
	@PastOrPresent(message = "식사 날짜는 현재거나 과거 날짜여야 합니다.")
	LocalDateTime date,

	@Schema(description = "식단 타입", example = "BREAKFAST", nullable = false)
	@NotNull(message = "식단 타입은 필수입니다.")
	DietType dietType,

	@Schema(description = "메모", example = "아 배부르다.", nullable = false)
	@Size(min = 1, max = 100, message = "메모는 100자 이하여야 합니다.")
	@NotBlank(message = "메모는 필수입니다.")
	String memo
) {

}
