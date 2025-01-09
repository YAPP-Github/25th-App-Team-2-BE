package com.tnt.domain.member;

import java.util.Map;

import com.tnt.dto.member.AppleUserInfo;
import com.tnt.dto.member.KakaoUserInfo;
import com.tnt.dto.member.OAuthUserInfo;

import lombok.Getter;

@Getter
public enum SocialType {
	KAKAO {
		@Override
		public OAuthUserInfo extractOAuthUserInfo(Map<String, Object> attributes) {
			return new KakaoUserInfo(attributes);
		}
	},
	APPLE {
		@Override
		public OAuthUserInfo extractOAuthUserInfo(Map<String, Object> attributes) {
			return new AppleUserInfo(attributes);
		}
	};

	public abstract OAuthUserInfo extractOAuthUserInfo(Map<String, Object> attributes);
}
