package com.tnt.domain.member;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.tnt.dto.member.AppleUserInfo;
import com.tnt.dto.member.KakaoUserInfo;
import com.tnt.dto.member.OAuthUserInfo;

@SpringBootTest
class SocialTypeTest {

	@Test
	@DisplayName("KAKAO 유저 정보 리턴 성공")
	void returns_kakao_user_info_success() {
		// given
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("id", 12345678);
		attributes.put("kakao_account", Map.of(
			"email", "test@kakao.com",
			"age", "20~29",
			"gender", "male"
		));
		attributes.put("properties", Map.of("nickname", "카카오닉네임"));

		// when
		OAuthUserInfo userInfo = SocialType.KAKAO.getOAuthUserInfo(attributes);

		// then
		assertThat(userInfo).isInstanceOf(KakaoUserInfo.class);
		assertThat(userInfo.getId()).isEqualTo("12345678");
		assertThat(userInfo.getEmail()).isEqualTo("test@kakao.com");
		assertThat(userInfo.getName()).isEqualTo("카카오닉네임");
	}

	@Test
	@DisplayName("APPLE 유저 정보 리턴 성공")
	void returns_apple_user_info_success() {
		// given
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("sub", "apple123.123.123");
		attributes.put("email", "test@privaterelay.appleid.com");
		attributes.put("name", Map.of(
			"firstName", "길동",
			"lastName", "홍"
		));

		// when
		OAuthUserInfo userInfo = SocialType.APPLE.getOAuthUserInfo(attributes);

		// then
		assertThat(userInfo).isInstanceOf(AppleUserInfo.class);
		assertThat(userInfo.getId()).isEqualTo("apple123.123.123");
		assertThat(userInfo.getEmail()).isEqualTo("test@privaterelay.appleid.com");
		assertThat(userInfo.getName()).isEqualTo("길동홍");
	}

	@Test
	@DisplayName("서로 다른 타입의 유저 정보 리턴 성공")
	void return_different_user_info_types_success() {
		// given
		Map<String, Object> attributes = new HashMap<>();

		// when
		OAuthUserInfo kakaoUserInfo = SocialType.KAKAO.getOAuthUserInfo(attributes);
		OAuthUserInfo appleUserInfo = SocialType.APPLE.getOAuthUserInfo(attributes);

		// then
		assertThat(kakaoUserInfo).isInstanceOf(KakaoUserInfo.class);
		assertThat(appleUserInfo).isInstanceOf(AppleUserInfo.class);
		assertThat(kakaoUserInfo).isNotInstanceOf(AppleUserInfo.class);
		assertThat(appleUserInfo).isNotInstanceOf(KakaoUserInfo.class);
	}
}
