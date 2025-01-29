package com.tnt.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AppleAuthTokenInfo {

	String accessToken;
	Integer expiresIn;
	String idToken;
	String refreshToken;
	String tokenType;
}
