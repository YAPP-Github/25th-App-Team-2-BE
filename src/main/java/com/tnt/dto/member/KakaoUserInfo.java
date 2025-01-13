package com.tnt.dto.member;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KakaoUserInfo extends OAuthUserInfo {

	private static final String KAKAO_ACCOUNT_KEY = "kakao_account";
	private static final String PROPERTIES_KEY = "properties";

	public KakaoUserInfo(Map<String, Object> attributes) {
		super(attributes);
		log.info("kakao attributes: {}", attributes);
	}

	@Override
	public String getId() {
		return String.valueOf(attributes.get("id"));
	}

	@Override
	public String getEmail() {
		return getAttributeFromAccount(KAKAO_ACCOUNT_KEY, "email");
	}

	@Override
	public String getName() {
		return getAttributeFromAccount(PROPERTIES_KEY, "nickname");
	}
}
