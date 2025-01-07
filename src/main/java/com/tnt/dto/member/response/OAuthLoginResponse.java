package com.tnt.dto.member.response;

import lombok.Builder;

@Builder
public record OAuthLoginResponse(
	String sessionId,

	String memberId,

	boolean isSignUp
) {

	public static OAuthLoginResponse of(String sessionId, String memberId, boolean isSignUp) {
		return new OAuthLoginResponse(sessionId, memberId, isSignUp);
	}
}
