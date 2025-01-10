package com.tnt.application.member;

import java.util.Map;

import com.tnt.dto.member.OAuthUserInfo;

public interface OAuthUserInfoExtractor {

	OAuthUserInfo extract(Map<String, Object> attributes);
}
