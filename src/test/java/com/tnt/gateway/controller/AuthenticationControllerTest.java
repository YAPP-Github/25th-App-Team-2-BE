package com.tnt.gateway.controller;

import static com.tnt.common.error.model.ErrorMessage.KAKAO_SERVER_ERROR;
import static com.tnt.common.error.model.ErrorMessage.MEMBER_NOT_FOUND;
import static com.tnt.common.error.model.ErrorMessage.UNSUPPORTED_SOCIAL_TYPE;
import static com.tnt.domain.member.MemberType.TRAINEE;
import static com.tnt.domain.member.MemberType.TRAINER;
import static com.tnt.domain.member.SocialType.APPLE;
import static com.tnt.domain.member.SocialType.KAKAO;
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
import org.springframework.security.core.context.SecurityContextHolder;

import com.tnt.application.member.MemberService;
import com.tnt.common.error.exception.NotFoundException;
import com.tnt.common.error.exception.OAuthException;
import com.tnt.dto.member.response.LogoutResponse;
import com.tnt.gateway.dto.request.OAuthLoginRequest;
import com.tnt.gateway.dto.response.CheckSessionResponse;
import com.tnt.gateway.dto.response.OAuthLoginResponse;
import com.tnt.gateway.service.OAuthService;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

	@InjectMocks
	private AuthenticationController authenticationController;

	@Mock
	private OAuthService oauthService;

	@Mock
	private MemberService memberService;

	@Test
	@DisplayName("로그인 세션 유효 확인 성공")
	void is_session_valid_success() {
		// given
		Long memberId = 1L;

		given(memberService.getMemberType(memberId)).willReturn(new CheckSessionResponse(TRAINER, true));

		// when
		CheckSessionResponse checkSessionResponse = authenticationController.checkSession(memberId);

		// then
		assertThat(checkSessionResponse.memberType()).isEqualTo(TRAINER);
	}

	@Nested
	@DisplayName("Login 테스트")
	class LoginTest {

		@Test
		@DisplayName("Kakao 로그인 성공")
		void kakao_login_success() {
			// given
			OAuthLoginRequest request = new OAuthLoginRequest(KAKAO, "fcm", "test-kakao-access-token", null);

			given(oauthService.oauthLogin(request)).willReturn(
				new OAuthLoginResponse("123456789", "", "", null, true, TRAINER));

			// when
			OAuthLoginResponse response = authenticationController.oauthLogin(request);

			// then
			assertThat(response.sessionId()).hasToString("123456789");
			verify(oauthService).oauthLogin(request);
		}

		@Test
		@DisplayName("Apple 로그인 성공")
		void apple_login_with_ios_success() {
			// given
			OAuthLoginRequest request = new OAuthLoginRequest(APPLE, "fcm", null, "test-id-token");

			given(oauthService.oauthLogin(request)).willReturn(
				new OAuthLoginResponse("123456789", "", "", null, true, TRAINEE));

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
			OAuthLoginRequest request = new OAuthLoginRequest(null, "fcm", "test-access-token", null);

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
			OAuthLoginRequest request = new OAuthLoginRequest(KAKAO, "fcm", "invalid-token", null);

			given(oauthService.oauthLogin(request)).willThrow(new OAuthException(KAKAO_SERVER_ERROR));

			// when & then
			assertThatThrownBy(() -> authenticationController.oauthLogin(request))
				.isInstanceOf(OAuthException.class)
				.hasMessage(KAKAO_SERVER_ERROR.getMessage());
		}

		@Test
		@DisplayName("신규 회원일 때 예외 발생")
		void member_not_found_error() {
			// given
			OAuthLoginRequest request = new OAuthLoginRequest(KAKAO, "fcm", "test-token", null);

			given(oauthService.oauthLogin(request)).willThrow(new NotFoundException(MEMBER_NOT_FOUND));

			// when & then
			assertThatThrownBy(() -> authenticationController.oauthLogin(request))
				.isInstanceOf(NotFoundException.class)
				.hasMessage(MEMBER_NOT_FOUND.getMessage());
		}
	}

	@Nested
	@DisplayName("Logout 테스트")
	class LogoutTest {

		@Test
		@DisplayName("로그아웃 성공")
		void logout_success() {
			// given
			Long memberId = 111L;
			String sessionId = "testSessionId";

			given(oauthService.logout(memberId)).willReturn(new LogoutResponse(sessionId));

			// when
			LogoutResponse response = authenticationController.logout(memberId);

			//then
			assertThat(response.sessionId()).isEqualTo(sessionId);
			assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		}
	}
}
