package com.tnt.fixture;

import java.time.LocalDate;

import com.tnt.domain.pt.PtTrainerTrainee;
import com.tnt.domain.trainee.Trainee;
import com.tnt.domain.trainer.Trainer;

public class PtTrainerTraineeFixture {

	public static PtTrainerTrainee getPtTrainerTrainee1(Trainer trainer, Trainee trainee) {
		LocalDate startDate = LocalDate.of(2025, 1, 1);
		Integer totalPtCount = 10;
		Integer finishedPtCount = 3;

		return PtTrainerTrainee.builder()
			.trainer(trainer)
			.trainee(trainee)
			.startedAt(startDate)
			.finishedPtCount(finishedPtCount)
			.totalPtCount(totalPtCount)
			.build();
	}

	public static PtTrainerTrainee getPtTrainerTrainee2(Trainer trainer, Trainee trainee) {
		LocalDate startDate = LocalDate.of(2025, 1, 1);
		Integer totalPtCount = 10;
		Integer finishedPtCount = 10;

		return PtTrainerTrainee.builder()
			.trainer(trainer)
			.trainee(trainee)
			.startedAt(startDate)
			.finishedPtCount(finishedPtCount)
			.totalPtCount(totalPtCount)
			.build();
	}
}
