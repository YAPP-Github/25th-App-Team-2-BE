package com.tnt.infrastructure.mysql.repository.pt;

import static com.tnt.domain.member.QMember.member;
import static com.tnt.domain.pt.QPtLesson.ptLesson;
import static com.tnt.domain.pt.QPtTrainerTrainee.ptTrainerTrainee;
import static com.tnt.domain.trainee.QTrainee.trainee;
import static com.tnt.domain.trainer.QTrainer.trainer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tnt.domain.pt.PtLesson;
import com.tnt.domain.pt.PtTrainerTrainee;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PtLessonSearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public List<PtLesson> findAllByTrainerIdAndDate(Long trainerId, LocalDate date) {
		return jpaQueryFactory
			.selectFrom(ptLesson)
			.join(ptLesson.ptTrainerTrainee, ptTrainerTrainee).fetchJoin()
			.join(ptTrainerTrainee.trainer, trainer).fetchJoin()
			.join(ptTrainerTrainee.trainee, trainee).fetchJoin()
			.join(trainee.member, member).fetchJoin()
			.where(
				trainer.id.eq(trainerId),
				ptLesson.lessonStart.between(date.atStartOfDay(), date.atTime(LocalTime.MAX)),
				ptLesson.deletedAt.isNull()
			)
			.orderBy(ptLesson.lessonStart.asc())
			.fetch();
	}

	public List<PtLesson> findAllByTraineeIdForCalendar(Long traineeId, Integer year, Integer month) {
		LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
		LocalDateTime endDate = startDate.plusMonths(1).minusNanos(1);

		return jpaQueryFactory
			.selectFrom(ptLesson)
			.join(ptLesson.ptTrainerTrainee, ptTrainerTrainee).fetchJoin()
			.join(ptTrainerTrainee.trainer, trainer).fetchJoin()
			.where(
				trainer.id.eq(traineeId),
				ptLesson.lessonStart.between(startDate, endDate),
				ptLesson.deletedAt.isNull()
			)
			.orderBy(ptLesson.lessonStart.asc())
			.fetch();
	}

	public boolean existsByStartAndEnd(PtTrainerTrainee pt, LocalDateTime start, LocalDateTime end) {
		return jpaQueryFactory
			.selectOne()
			.from(ptLesson)
			.join(ptLesson.ptTrainerTrainee, ptTrainerTrainee)
			.where(
				ptTrainerTrainee.eq(pt),
				ptLesson.lessonStart.lt(end),
				ptLesson.lessonEnd.gt(start),
				ptLesson.deletedAt.isNull()
			)
			.fetchFirst() != null;
	}
}
