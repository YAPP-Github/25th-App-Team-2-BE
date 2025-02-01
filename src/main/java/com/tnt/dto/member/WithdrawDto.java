package com.tnt.dto.member;

import com.tnt.domain.member.SocialType;

public record WithdrawDto(
	String socialId,
	SocialType socialType,
	String profileImageUrl
) {

}
