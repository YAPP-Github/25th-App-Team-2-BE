package com.tnt.infrastructure.mysql.repository.trainee;

import static com.tnt.domain.trainee.QDiet.diet;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tnt.domain.trainee.Diet;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DietSearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public List<Diet> findAllByTraineeIdForDaily(Long traineeId, LocalDate date) {
		return jpaQueryFactory
			.selectFrom(diet)
			.where(
				diet.traineeId.eq(traineeId),
				diet.date.between(date.atStartOfDay(), date.plusDays(1).atStartOfDay()),
				diet.deletedAt.isNull()
			)
			.orderBy(diet.date.asc())
			.fetch();
	}

	public List<Diet> findAllByTraineeIdForTraineeCalendar(Long traineeId, LocalDate startDate, LocalDate endDate) {
		return jpaQueryFactory
			.selectFrom(diet)
			.where(
				diet.traineeId.eq(traineeId),
				diet.date.between(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay()),
				diet.deletedAt.isNull()
			)
			.orderBy(diet.date.asc())
			.fetch();
	}
}
