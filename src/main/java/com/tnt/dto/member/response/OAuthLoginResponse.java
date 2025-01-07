package com.tnt.dto.member.response;

import lombok.Builder;

@Builder
public record OAuthLoginResponse(
	String sessionId
) {

	public static OAuthLoginResponse from(String sessionId) {
		return new OAuthLoginResponse(sessionId);
	}
}
