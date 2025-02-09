package com.tnt.dto.trainer.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리중인 트레이니 목록 응답")
public record GetActiveTraineesResponse(
	@Schema(description = "트레이니 목록", nullable = false)
	List<TraineeDto> trainees
) {

	public record TraineeDto(
		@Schema(description = "트레이니 ID", example = "123523564", nullable = false)
		Long id,

		@Schema(description = "트레이니 이름", example = "김정호", nullable = false)
		String name
	) {

	}
}
