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
		Integer session,
		LocalDateTime lessonStart,
		LocalDateTime lessonEnd
	) {

	}
}
