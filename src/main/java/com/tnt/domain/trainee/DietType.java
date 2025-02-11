package com.tnt.domain.trainee;

import static com.tnt.common.error.model.ErrorMessage.UNSUPPORTED_DIET_TYPE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.tnt.common.error.exception.TnTException;

public enum DietType {
	BREAKFAST,
	LUNCH,
	DINNER,
	SNACK;

	@JsonCreator
	public static DietType of(String value) {
		for (DietType type : DietType.values()) {
			if (type.name().equalsIgnoreCase(value)) { // 대소문자 구분 없이 처리
				return type;
			}
		}
		throw new TnTException(UNSUPPORTED_DIET_TYPE);
	}
}
