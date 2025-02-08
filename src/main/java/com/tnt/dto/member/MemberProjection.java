package com.tnt.dto.member;

import java.time.LocalDate;
import java.util.List;

import com.querydsl.core.annotations.QueryProjection;
import com.tnt.domain.member.MemberType;
import com.tnt.domain.member.SocialType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberProjection {

	@QueryProjection
	public record MemberTypeDto(MemberType memberType) {

	}

	@QueryProjection
	public record MemberInfoDto(
		String name,
		String email,
		String profileImageUrl,
		LocalDate birthday,
		MemberType memberType,
		SocialType socialType,
		Integer activeTraineeCount,
		Integer previousTraineeCount,
		String invitationCode,
		Long trainerId,
		Double height,
		Double weight,
		String cautionNote,
		List<String> goalContents
	) {

	}
}
