package com.tnt.infrastructure.mysql.repository.pt;

import static com.tnt.domain.member.QMember.member;
import static com.tnt.domain.pt.QPtTrainerTrainee.ptTrainerTrainee;
import static com.tnt.domain.trainee.QTrainee.trainee;
import static com.tnt.domain.trainer.QTrainer.trainer;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tnt.domain.trainee.Trainee;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PtTrainerTraineeSearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public List<Trainee> findAllTrainees(Long trainerId) {
		return jpaQueryFactory
			.select(ptTrainerTrainee.trainee)
			.from(ptTrainerTrainee)
			.join(ptTrainerTrainee.trainer, trainer)
			.join(ptTrainerTrainee.trainee, trainee)
			.join(trainee.member, member)
			.where(
				ptTrainerTrainee.trainer.id.eq(trainerId),
				ptTrainerTrainee.deletedAt.isNull()
			)
			.limit(10)
			.fetch();
	}
}
