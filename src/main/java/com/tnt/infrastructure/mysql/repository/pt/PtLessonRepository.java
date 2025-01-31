package com.tnt.infrastructure.mysql.repository.pt;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tnt.domain.pt.PtLesson;
import com.tnt.domain.pt.PtTrainerTrainee;

public interface PtLessonRepository extends JpaRepository<PtLesson, Long> {

	Optional<List<PtLesson>> findAllByPtTrainerTraineeAndDeletedAtIsNull(PtTrainerTrainee ptTrainerTrainee);
}
