package com.tnt.infrastructure.mysql.repository.member;

import static com.tnt.domain.member.QMember.member;
import static com.tnt.domain.pt.QPtTrainerTrainee.ptTrainerTrainee;
import static com.tnt.domain.trainee.QPtGoal.ptGoal;
import static com.tnt.domain.trainee.QTrainee.trainee;
import static com.tnt.domain.trainer.QTrainer.trainer;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tnt.dto.member.MemberProjection;
import com.tnt.dto.member.QMemberProjection_MemberInfoDto;
import com.tnt.dto.member.QMemberProjection_MemberTypeDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberSearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public Optional<MemberProjection.MemberInfoDto> findMemberInfo(Long memberId) {
		Map<Long, MemberProjection.MemberInfoDto> result = jpaQueryFactory
			.from(member)
			.leftJoin(trainer).on(trainer.member.id.eq(member.id), trainer.deletedAt.isNull())
			.leftJoin(trainee).on(trainee.member.id.eq(member.id), trainee.deletedAt.isNull())
			.leftJoin(ptGoal).on(ptGoal.traineeId.eq(trainee.id), ptGoal.deletedAt.isNull())
			.leftJoin(ptTrainerTrainee).on(ptTrainerTrainee.trainer.eq(trainer), ptTrainerTrainee.deletedAt.isNull())
			.where(
				member.id.eq(memberId),
				member.deletedAt.isNull()
			)
			.transform(GroupBy.groupBy(member.id).as(
				new QMemberProjection_MemberInfoDto(member.name, member.email, member.profileImageUrl, member.birthday,
					member.memberType, member.socialType,
					Expressions.as(
						JPAExpressions.select(ptTrainerTrainee.trainee.countDistinct().intValue())
							.from(ptTrainerTrainee)
							.where(
								ptTrainerTrainee.trainer.eq(trainer),
								ptTrainerTrainee.deletedAt.isNull()
							),
						"activeTraineeCount"
					),
					Expressions.as(
						JPAExpressions.select(ptTrainerTrainee.trainee.countDistinct().intValue())
							.from(ptTrainerTrainee)
							.where(ptTrainerTrainee.trainer.eq(trainer)),
						"totalTraineeCount"
					),
					trainer.invitationCode, trainer.id, trainee.height, trainee.weight, trainee.cautionNote,
					GroupBy.list(ptGoal.content)
				)
			));

		return Optional.ofNullable(result.get(memberId));
	}

	public Optional<MemberProjection.MemberTypeDto> findMemberType(Long memberId) {
		return Optional.ofNullable(jpaQueryFactory
			.select(new QMemberProjection_MemberTypeDto(member.memberType))
			.from(member)
			.where(
				member.id.eq(memberId),
				member.deletedAt.isNull()
			)
			.fetchOne());
	}
}
