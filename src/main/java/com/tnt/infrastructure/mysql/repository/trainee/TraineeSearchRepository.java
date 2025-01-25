package com.tnt.infrastructure.mysql.repository.trainee;

import static com.tnt.domain.member.QMember.member;
import static com.tnt.domain.trainee.QTrainee.trainee;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tnt.domain.trainee.Trainee;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TraineeSearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public Optional<Trainee> findByMemberIdAndDeletedAtIsNull(Long memberId) {
		return Optional.ofNullable(jpaQueryFactory
			.selectFrom(trainee)
			.join(trainee.member, member).fetchJoin()
			.where(
				member.id.eq(memberId),
				trainee.deletedAt.isNull()
			)
			.fetchOne());
	}
}
