package com.tnt.dto.member;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppleUserInfo extends OAuthUserInfo {

	public AppleUserInfo(final Map<String, Object> attributes) {
		super(attributes);
		log.info("apple attributes: {}", attributes);
	}

	@Override
	public String getId() {
		return String.valueOf(attributes.get("sub"));
	}

	@Override
	public String getEmail() {
		return String.valueOf(attributes.get("email"));
	}

	@Override
	public String getAge() {
		return null; // Apple에서 나이 제공 안 함
	}

	@Override
	public String getGender() {
		return null; // Apple에서 성별 제공 안 함
	}

	@Override
	public String getName() {
		String firstName = getAttributeFromAccount("name", "firstName");
		String lastName = getAttributeFromAccount("name", "lastName");

		return firstName + lastName;
	}
}