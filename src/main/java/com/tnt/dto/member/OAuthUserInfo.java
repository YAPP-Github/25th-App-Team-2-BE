package com.tnt.dto.member;

import java.util.Map;

public abstract class OAuthUserInfo {

	protected final Map<String, Object> attributes;

	protected OAuthUserInfo(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public abstract String getId();

	public abstract String getEmail();

	public abstract String getAge();

	public abstract String getGender();

	public abstract String getName();

	protected String getAttributeFromAccount(String infoKey, String attributeKey) {
		Map<String, Object> attribute = getAttributeAsMap(infoKey);

		if (attribute == null) {
			return null;
		}

		return String.valueOf(attribute.get(attributeKey));
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getAttributeAsMap(String infoKey) {
		return (Map<String, Object>)attributes.get(infoKey);
	}
}
