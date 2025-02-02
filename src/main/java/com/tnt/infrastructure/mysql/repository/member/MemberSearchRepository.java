package com.tnt.infrastructure.mysql.repository.member;

import static com.tnt.domain.member.QMember.member;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tnt.dto.member.MemberProjection;
import com.tnt.dto.member.QMemberProjection_MemberTypeDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberSearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public Optional<MemberProjection.MemberTypeDto> findMemberTypeByMemberId(Long memberId) {
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
