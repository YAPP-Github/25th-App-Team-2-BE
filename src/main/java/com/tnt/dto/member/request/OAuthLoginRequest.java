package com.tnt.dto.member.request;

import jakarta.validation.constraints.NotBlank;

public record OAuthLoginRequest(
	@NotBlank
	String socialType,
	String socialAccessToken,
	String authorizationCode,
	String idToken
) {

}
