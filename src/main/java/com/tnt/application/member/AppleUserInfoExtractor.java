package com.tnt.application.member;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.tnt.dto.member.AppleUserInfo;
import com.tnt.dto.member.OAuthUserInfo;

@Component
public class AppleUserInfoExtractor implements OAuthUserInfoExtractor {

	public OAuthUserInfo extract(Map<String, Object> attributes) {
		return new AppleUserInfo(attributes);
	}
}
