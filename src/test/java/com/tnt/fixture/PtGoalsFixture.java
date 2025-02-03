package com.tnt.fixture;

import java.util.List;

import com.tnt.domain.trainee.PtGoal;

public class PtGoalsFixture {

	public static List<PtGoal> getPtGoals(Long traineeId) {
		return List.of(
			PtGoal.builder()
				.traineeId(traineeId)
				.content("체중 감량")
				.build(),
			PtGoal.builder()
				.traineeId(traineeId)
				.content("근력 향상")
				.build()
		);
	}
}
