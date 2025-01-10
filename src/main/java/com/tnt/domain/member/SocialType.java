package com.tnt.domain.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SocialType {

	KAKAO("kakao"),

	APPLE("apple");

	private final String registrationId;
}
