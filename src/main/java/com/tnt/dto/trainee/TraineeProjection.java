package com.tnt.dto.trainee;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TraineeProjection {

	@QueryProjection
	public record PtInfoDto(
		String trainerName,
		LocalDateTime lessonStart,
		LocalDateTime lessonEnd
	) {

	}

	@QueryProjection
	public record PtCountInfoDto(
		Integer finishedPtCount,
		Integer totalPtCount
	) {

	}
}
