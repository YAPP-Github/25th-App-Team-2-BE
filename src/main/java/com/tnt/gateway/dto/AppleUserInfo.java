package com.tnt.gateway.dto;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppleUserInfo extends OAuthUserInfo {

	public AppleUserInfo(Map<String, Object> attributes) {
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
	public String getName() {
		String firstName = getAttributeFromAccount("name", "firstName");
		String lastName = getAttributeFromAccount("name", "lastName");

		return firstName + lastName;
	}
}
