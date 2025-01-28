package com.tnt.gateway.dto;

import java.util.Map;
import java.util.Objects;

public abstract class OAuthUserInfo {

	protected final Map<String, Object> attributes;

	protected OAuthUserInfo(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public abstract String getId();

	public abstract String getEmail();

	public abstract String getName();

	protected String getAttributeFromAccount(String infoKey, String attributeKey) {
		Map<String, Object> attribute = getAttributeAsMap(infoKey);

		if (Objects.isNull(attribute)) {
			return null;
		}

		return String.valueOf(attribute.get(attributeKey));
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getAttributeAsMap(String infoKey) {
		return (Map<String, Object>)attributes.get(infoKey);
	}
}
