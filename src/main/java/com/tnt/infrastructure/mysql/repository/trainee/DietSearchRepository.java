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

	public List<Diet> findAllByTraineeIdForDaily(Long traineeId, Integer year, Integer month) {
		LocalDate startOfMonth = LocalDate.of(year, month, 1);
		LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

		return jpaQueryFactory
			.selectFrom(diet)
			.where(
				diet.traineeId.eq(traineeId),
				diet.date.goe(startOfMonth.atStartOfDay()),
				diet.date.lt(endOfMonth.plusDays(1).atStartOfDay()),
				diet.deletedAt.isNull()
			)
			.orderBy(diet.date.asc())
			.fetch();
	}
}
