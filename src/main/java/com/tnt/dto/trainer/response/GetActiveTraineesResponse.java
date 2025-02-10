package com.tnt.dto.trainer.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리중인 트레이니 목록 응답")
public record GetActiveTraineesResponse(
	@Schema(description = "트레이니 회원 수", nullable = false)
	Integer traineeCount,

	@Schema(description = "트레이니 목록", nullable = false)
	List<TraineeInfo> trainees
) {

	public record TraineeInfo(
		@Schema(description = "트레이니 ID", example = "123523564", nullable = false)
		Long id,

		@Schema(description = "트레이니 이름", example = "김정호", nullable = false)
		String name,

		@Schema(description = "진행한 PT 횟수", example = "10", nullable = false)
		Integer finishedPtCount,

		@Schema(description = "총 PT 횟수", example = "100", nullable = false)
		Integer totalPtCount,

		@Schema(description = "주의사항", example = "가냘퍼요", nullable = true)
		String cautionNote,

		@Schema(description = "PT 목적들", example = "[\"체중 감량\", \"근력 향상\"]", nullable = false)
		List<String> goalContents
	) {

	}
}
