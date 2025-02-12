package com.tnt.dto.member;

import com.querydsl.core.annotations.QueryProjection;
import com.tnt.domain.member.MemberType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberProjection {

	@QueryProjection
	public record MemberTypeDto(MemberType memberType) {

	}
}
