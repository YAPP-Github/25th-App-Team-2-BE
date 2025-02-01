package com.tnt.fixture;

import java.time.LocalDateTime;
import java.util.List;

import com.tnt.domain.pt.PtLesson;
import com.tnt.domain.pt.PtTrainerTrainee;

public class PtLessonsFixture {

	public static List<PtLesson> getPtLessons(PtTrainerTrainee ptTrainerTrainee) {
		return List.of(PtLesson.builder()
			.id(1L)
			.ptTrainerTrainee(ptTrainerTrainee)
			.lessonStart(LocalDateTime.now())
			.lessonEnd(LocalDateTime.now())
			.build());
	}
}
