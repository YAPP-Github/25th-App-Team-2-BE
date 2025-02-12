package com.tnt.fixture;

import java.time.LocalDateTime;
import java.util.List;

import com.tnt.domain.pt.PtLesson;
import com.tnt.domain.pt.PtTrainerTrainee;

public class PtLessonsFixture {

	public static List<PtLesson> getPtLessons1WithId(PtTrainerTrainee ptTrainerTrainee) {
		LocalDateTime startDate1 = LocalDateTime.parse("2025-02-01T11:30");
		LocalDateTime endDate1 = LocalDateTime.parse("2025-02-01T13:00");

		LocalDateTime startDate2 = LocalDateTime.parse("2025-02-02T11:30");
		LocalDateTime endDate2 = LocalDateTime.parse("2025-02-02T13:00");

		return List.of(PtLesson.builder()
				.id(1L)
				.ptTrainerTrainee(ptTrainerTrainee)
				.session(1)
				.lessonStart(startDate1)
				.lessonEnd(endDate1)
				.build(),
			PtLesson.builder()
				.id(2L)
				.ptTrainerTrainee(ptTrainerTrainee)
				.session(2)
				.lessonStart(startDate2)
				.lessonEnd(endDate2)
				.build()
		);
	}

	public static List<PtLesson> getPtLessons1(PtTrainerTrainee ptTrainerTrainee) {
		LocalDateTime startDate1 = LocalDateTime.parse("2025-02-01T11:30");
		LocalDateTime endDate1 = LocalDateTime.parse("2025-02-01T13:00");

		LocalDateTime startDate2 = LocalDateTime.parse("2025-02-02T11:30");
		LocalDateTime endDate2 = LocalDateTime.parse("2025-02-02T13:00");

		return List.of(PtLesson.builder()
				.ptTrainerTrainee(ptTrainerTrainee)
				.session(3)
				.lessonStart(startDate1)
				.lessonEnd(endDate1)
				.build(),
			PtLesson.builder()
				.ptTrainerTrainee(ptTrainerTrainee)
				.session(4)
				.lessonStart(startDate2)
				.lessonEnd(endDate2)
				.build()
		);
	}
}
