package com.tnt.presentation.member;

import static com.tnt.global.error.model.ErrorMessage.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tnt.application.member.OAuthService;
import com.tnt.dto.member.request.OAuthLoginRequest;
import com.tnt.dto.member.response.OAuthLoginResponse;
import com.tnt.global.error.exception.NotFoundException;
import com.tnt.global.error.exception.OAuthException;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

	@Mock
	private OAuthService oauthService;

	@InjectMocks
	private LoginController loginController;

	@Test
	@DisplayName("Kakao 로그인 성공")
	void kakao_login_success() {
		// given
		OAuthLoginRequest request = new OAuthLoginRequest(
			"KAKAO",
			"test-kakao-access-token",
			null,
			null
		);

		given(oauthService.oauthLogin(request)).willReturn(new OAuthLoginResponse("123456789", "", true));

		// when
		OAuthLoginResponse response = loginController.oauthLogin(request);

		// then
		assertThat(response.sessionId()).hasToString("123456789");
		verify(oauthService).oauthLogin(request);
	}

	@Test
	@DisplayName("ANDROID - Apple 로그인 성공")
	void apple_login_with_android_success() {
		// given
		OAuthLoginRequest request = new OAuthLoginRequest(
			"APPLE",
			null,
			null,
			"test-id-token"
		);

		given(oauthService.oauthLogin(request)).willReturn(new OAuthLoginResponse("123456789", "", true));

		// when
		OAuthLoginResponse response = loginController.oauthLogin(request);

		// then
		assertThat(response.sessionId()).isEqualTo("123456789");
		verify(oauthService).oauthLogin(request);
	}

	@Test
	@DisplayName("iOS - Apple 로그인 성공")
	void apple_login_with_ios_success() {
		// given
		OAuthLoginRequest request = new OAuthLoginRequest(
			"APPLE",
			null,
			"test-authorization-code",
			null
		);

		given(oauthService.oauthLogin(request)).willReturn(new OAuthLoginResponse("123456789", "", true));

		// when
		OAuthLoginResponse response = loginController.oauthLogin(request);

		// then
		assertThat(response.sessionId()).isEqualTo("123456789");
		verify(oauthService).oauthLogin(request);
	}

	@Test
	@DisplayName("지원하지 않는 소셜 타입일 경우 예외 발생")
	void unsupported_social_type_error() {
		// given
		OAuthLoginRequest request = new OAuthLoginRequest(
			"NAVER",
			"test-access-token",
			null,
			null
		);

		given(oauthService.oauthLogin(request)).willThrow(new OAuthException(UNSUPPORTED_SOCIAL_TYPE));

		// when & then
		assertThatThrownBy(() -> loginController.oauthLogin(request))
			.isInstanceOf(OAuthException.class)
			.hasMessage(UNSUPPORTED_SOCIAL_TYPE.getMessage());
	}

	@Test
	@DisplayName("OAuth 서버 에러 시 예외 발생")
	void oauth_server_error() {
		// given
		OAuthLoginRequest request = new OAuthLoginRequest(
			"KAKAO",
			"invalid-token",
			null,
			null
		);

		given(oauthService.oauthLogin(request)).willThrow(new OAuthException(FAILED_TO_FETCH_USER_INFO));

		// when & then
		assertThatThrownBy(() -> loginController.oauthLogin(request))
			.isInstanceOf(OAuthException.class)
			.hasMessage(FAILED_TO_FETCH_USER_INFO.getMessage());
	}

	@Test
	@DisplayName("신규 회원일 때 예외 발생")
	void member_not_found_error() {
		// given
		OAuthLoginRequest request = new OAuthLoginRequest(
			"KAKAO",
			"test-token",
			null,
			null
		);

		given(oauthService.oauthLogin(request)).willThrow(new NotFoundException(MEMBER_NOT_FOUND));

		// when & then
		assertThatThrownBy(() -> loginController.oauthLogin(request))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(MEMBER_NOT_FOUND.getMessage());
	}
}
