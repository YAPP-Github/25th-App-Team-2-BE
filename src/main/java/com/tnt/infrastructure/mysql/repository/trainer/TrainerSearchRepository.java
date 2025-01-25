package com.tnt.infrastructure.mysql.repository.trainer;

import static com.tnt.domain.member.QMember.member;
import static com.tnt.domain.trainer.QTrainer.trainer;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tnt.domain.trainer.Trainer;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TrainerSearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public Optional<Trainer> findByMemberIdAndDeletedAtIsNull(Long memberId) {
		return Optional.ofNullable(jpaQueryFactory
			.selectFrom(trainer)
			.join(trainer.member, member).fetchJoin()
			.where(
				member.id.eq(memberId),
				trainer.deletedAt.isNull()
			)
			.fetchOne());
	}

	public Optional<Trainer> findByInvitationCodeAndDeletedAtIsNull(String invitationCode) {
		return Optional.ofNullable(jpaQueryFactory
			.selectFrom(trainer)
			.join(trainer.member, member).fetchJoin()
			.where(
				trainer.invitationCode.eq(invitationCode),
				trainer.deletedAt.isNull()
			)
			.fetchOne());
	}
}
