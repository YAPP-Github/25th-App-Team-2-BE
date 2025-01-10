package com.tnt.application.member;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.tnt.dto.member.KakaoUserInfo;
import com.tnt.dto.member.OAuthUserInfo;

@Component
public class KakaoUserInfoExtractor implements OAuthUserInfoExtractor {

	public OAuthUserInfo extract(Map<String, Object> attributes) {
		return new KakaoUserInfo(attributes);
	}
}
