package com.tnt.domain.member;

import static com.tnt.global.error.model.ErrorMessage.UNSUPPORTED_MEMBER_TYPE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.tnt.global.error.exception.TnTException;

public enum MemberType {
	UNREGISTERED,
	TRAINER,
	TRAINEE;

	@JsonCreator
	public static MemberType of(String value) {
		for (MemberType type : MemberType.values()) {
			if (type.name().equalsIgnoreCase(value)) { // 대소문자 구분 없이 처리
				return type;
			}
		}
		throw new TnTException(UNSUPPORTED_MEMBER_TYPE);
	}
}
