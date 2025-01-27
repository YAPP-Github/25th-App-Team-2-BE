package com.tnt.domain.member;

import static com.tnt.global.error.model.ErrorMessage.UNSUPPORTED_SOCIAL_TYPE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.tnt.global.error.exception.TnTException;

public enum SocialType {
	KAKAO,
	APPLE;

	@JsonCreator
	public static SocialType of(String value) {
		for (SocialType type : SocialType.values()) {
			if (type.name().equalsIgnoreCase(value)) { // 대소문자 구분 없이 처리
				return type;
			}
		}
		throw new TnTException(UNSUPPORTED_SOCIAL_TYPE);
	}
}
