package com.tnt.presentation.member;

import static com.tnt.global.error.model.ErrorMessage.FAILED_TO_FETCH_USER_INFO;
import static com.tnt.global.error.model.ErrorMessage.MEMBER_NOT_FOUND;
import static com.tnt.global.error.model.ErrorMessage.UNSUPPORTED_SOCIAL_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
class AuthenticationControllerTest {

	@Mock
	private OAuthService oauthService;

	@InjectMocks
	private AuthenticationController authenticationController;

	@Test
	@DisplayName("Kakao 로그인 성공")
	void kakao_login_success() {
		// given
		OAuthLoginRequest request = new OAuthLoginRequest("KAKAO", "fcm", "test-kakao-access-token", null, null);

		given(oauthService.oauthLogin(request)).willReturn(
			new OAuthLoginResponse("123456789", "", "", null, true));

		// when
		OAuthLoginResponse response = authenticationController.oauthLogin(request);

		// then
		assertThat(response.sessionId()).hasToString("123456789");
		verify(oauthService).oauthLogin(request);
	}

	@Test
	@DisplayName("ANDROID - Apple 로그인 성공")
	void apple_login_with_android_success() {
		// given
		OAuthLoginRequest request = new OAuthLoginRequest("APPLE", "fcm", null, null, "test-id-token");

		given(oauthService.oauthLogin(request)).willReturn(new OAuthLoginResponse("123456789", "", "", null, true));

		// when
		OAuthLoginResponse response = authenticationController.oauthLogin(request);

		// then
		assertThat(response.sessionId()).isEqualTo("123456789");
		verify(oauthService).oauthLogin(request);
	}

	@Test
	@DisplayName("iOS - Apple 로그인 성공")
	void apple_login_with_ios_success() {
		// given
		OAuthLoginRequest request = new OAuthLoginRequest("APPLE", "fcm", null, "test-authorization-code", null);

		given(oauthService.oauthLogin(request)).willReturn(
			new OAuthLoginResponse("123456789", "", "", null, true));

		// when
		OAuthLoginResponse response = authenticationController.oauthLogin(request);

		// then
		assertThat(response.sessionId()).isEqualTo("123456789");
		verify(oauthService).oauthLogin(request);
	}

	@Test
	@DisplayName("지원하지 않는 소셜 타입일 경우 예외 발생")
	void unsupported_social_type_error() {
		// given
		OAuthLoginRequest request = new OAuthLoginRequest("NAVER", "fcm", "test-access-token", null, null);

		given(oauthService.oauthLogin(request)).willThrow(new OAuthException(UNSUPPORTED_SOCIAL_TYPE));

		// when & then
		assertThatThrownBy(() -> authenticationController.oauthLogin(request))
			.isInstanceOf(OAuthException.class)
			.hasMessage(UNSUPPORTED_SOCIAL_TYPE.getMessage());
	}

	@Test
	@DisplayName("OAuth 서버 에러 시 예외 발생")
	void oauth_server_error() {
		// given
		OAuthLoginRequest request = new OAuthLoginRequest("KAKAO", "fcm", "invalid-token", null, null);

		given(oauthService.oauthLogin(request)).willThrow(new OAuthException(FAILED_TO_FETCH_USER_INFO));

		// when & then
		assertThatThrownBy(() -> authenticationController.oauthLogin(request))
			.isInstanceOf(OAuthException.class)
			.hasMessage(FAILED_TO_FETCH_USER_INFO.getMessage());
	}

	@Test
	@DisplayName("신규 회원일 때 예외 발생")
	void member_not_found_error() {
		// given
		OAuthLoginRequest request = new OAuthLoginRequest("KAKAO", "fcm", "test-token", null, null);

		given(oauthService.oauthLogin(request)).willThrow(new NotFoundException(MEMBER_NOT_FOUND));

		// when & then
		assertThatThrownBy(() -> authenticationController.oauthLogin(request))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(MEMBER_NOT_FOUND.getMessage());
	}

	@Nested
	@DisplayName("OAuthLoginRequest Validation 테스트")
	class OAuthLoginRequestValidationTest {

		@Test
		@DisplayName("카카오 로그인 시 socialAccessToken이 없으면 실패")
		void kakao_login_without_access_token_fail() {
			// given
			OAuthLoginRequest request = new OAuthLoginRequest("KAKAO", "fcm-token", null, null, null);

			// when & then
			assertThat(request.validateKakaoLogin()).isFalse();
		}

		@Test
		@DisplayName("카카오 로그인 시 socialAccessToken이 blank이면 실패")
		void kakao_login_with_blank_access_token_fail() {
			// given
			OAuthLoginRequest request = new OAuthLoginRequest("KAKAO", "fcm-token", " ", null, null);

			// when & then
			assertThat(request.validateKakaoLogin()).isFalse();
		}

		@Test
		@DisplayName("카카오 로그인 시 socialAccessToken이 있으면 성공")
		void kakao_login_with_access_token_success() {
			// given
			OAuthLoginRequest request = new OAuthLoginRequest("KAKAO", "fcm-token", "valid-token", null, null);

			// when & then
			assertThat(request.validateKakaoLogin()).isTrue();
		}

		@Test
		@DisplayName("애플 로그인 시 authorizationCode와 idToken이 모두 없으면 실패")
		void apple_login_without_code_and_token_fail() {
			// given
			OAuthLoginRequest request = new OAuthLoginRequest("APPLE", "fcm-token", null, null, null);

			// when & then
			assertThat(request.validateAppleLogin()).isFalse();
		}

		@Test
		@DisplayName("애플 로그인 시 authorizationCode만 있어도 성공")
		void apple_login_with_authorization_code_success() {
			// given
			OAuthLoginRequest request = new OAuthLoginRequest("APPLE", "fcm-token", null, "valid-auth-code", null);

			// when & then
			assertThat(request.validateAppleLogin()).isTrue();
		}

		@Test
		@DisplayName("애플 로그인 시 idToken만 있어도 성공")
		void apple_login_with_id_token_success() {
			// given
			OAuthLoginRequest request = new OAuthLoginRequest("APPLE", "fcm-token", null, null, "valid-id-token");

			// when & then
			assertThat(request.validateAppleLogin()).isTrue();
		}

		@Test
		@DisplayName("애플 로그인 시 authorizationCode와 idToken이 모두 있어도 성공")
		void apple_login_with_code_and_token_success() {
			// given
			OAuthLoginRequest request = new OAuthLoginRequest("APPLE", "fcm-token", null, "valid-auth-code",
				"valid-id-token");

			// when & then
			assertThat(request.validateAppleLogin()).isTrue();
		}

		@Test
		@DisplayName("애플 로그인 시 blank 토큰은 실패")
		void apple_login_with_blank_token_fail() {
			// given
			OAuthLoginRequest request = new OAuthLoginRequest("APPLE", "fcm-token", null, " ", " ");

			// when & then
			assertThat(request.validateAppleLogin()).isFalse();
		}
	}
}
