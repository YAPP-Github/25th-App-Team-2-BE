package com.tnt.fixture;

import java.time.LocalDate;

import com.tnt.domain.pt.PtTrainerTrainee;

public class PtTrainerTraineeFixture {

	public static PtTrainerTrainee getPtTrainerTrainee1(Long trainerId, Long traineeId) {
		LocalDate startDate = LocalDate.of(2025, 1, 1);
		Integer totalPtCount = 10;
		Integer finishedPtCount = 3;

		return PtTrainerTrainee.builder()
			.trainerId(trainerId)
			.traineeId(traineeId)
			.startedAt(startDate)
			.finishedPtCount(finishedPtCount)
			.totalPtCount(totalPtCount)
			.build();
	}
}
