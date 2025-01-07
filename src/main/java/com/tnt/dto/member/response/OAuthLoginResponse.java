package com.tnt.dto.member.response;

import lombok.Builder;

@Builder
public record OAuthLoginResponse(
	String memberId
) {

	public static OAuthLoginResponse from(String memberId) {
		return new OAuthLoginResponse(memberId);
	}
}
