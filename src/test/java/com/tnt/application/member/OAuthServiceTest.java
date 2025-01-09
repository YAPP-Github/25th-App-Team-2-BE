package com.tnt.application.member;

import static com.tnt.global.error.model.ErrorMessage.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.tnt.application.auth.SessionService;
import com.tnt.domain.member.Member;
import com.tnt.domain.member.SocialType;
import com.tnt.dto.member.request.OAuthLoginRequest;
import com.tnt.dto.member.response.OAuthLoginResponse;
import com.tnt.global.error.exception.NotFoundException;
import com.tnt.global.error.exception.OAuthException;
import com.tnt.infrastructure.mysql.member.repository.MemberRepository;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@ExtendWith(MockitoExtension.class)
class OAuthServiceTest {

	private MockWebServer mockWebServer;
	private OAuthService oAuthService;

	@Mock
	private SessionService sessionService;

	@Mock
	private MemberRepository memberRepository;

	@Value("${social-login.provider.apple.audience}")
	private String appleApiUrl;

	@BeforeEach
	void setUp() throws IOException {
		mockWebServer = new MockWebServer();
		mockWebServer.start();

		WebClient webClient = WebClient.builder()
			.baseUrl(mockWebServer.url("/").toString())
			.build();

		oAuthService = new OAuthService(webClient, sessionService, memberRepository);
	}

	@AfterEach
	void tearDown() throws IOException {
		mockWebServer.shutdown();
	}

	@Test
	@DisplayName("존재하지 않는 회원 예외 발생")
	void member_not_found_error() {
		// given
		OAuthLoginRequest request = new OAuthLoginRequest("KAKAO", "kakao-access-token", null, null);

		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(200)  // 성공 응답으로 설정
			.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.setBody("{\"id\": \"12345\", \"kakao_account\": {\"email\": \"test@example.com\"}}"));

		given(memberRepository.findBySocialIdAndSocialType("12345", SocialType.KAKAO))
			.willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> oAuthService.oauthLogin(request))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(MEMBER_NOT_FOUND.getMessage());
	}

	@Test
	@DisplayName("지원하지 않는 소셜 타입 예외 발생")
	void unsupported_social_type_error() {
		// given
		OAuthLoginRequest request = new OAuthLoginRequest("NAVER", "some-token", null, null);

		// when & then
		assertThatThrownBy(() -> oAuthService.oauthLogin(request))
			.isInstanceOf(OAuthException.class)
			.hasMessage(UNSUPPORTED_SOCIAL_TYPE.getMessage());
	}

	@Test
	@DisplayName("Kakao 로그인 실패 시 예외 발생")
	void kakao_login_failure_error() {
		// given
		OAuthLoginRequest request = new OAuthLoginRequest("KAKAO", "invalid-token", null, null);

		String errorResponse = "{\"msg\": \"this access token does not exist\", \"code\": -401}";

		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(401)
			.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.setBody(errorResponse));

		// when & then
		assertThatThrownBy(() -> oAuthService.oauthLogin(request))
			.isInstanceOf(OAuthException.class)
			.hasMessage(FAILED_TO_FETCH_USER_INFO.getMessage());
	}

	@Test
	@DisplayName("Apple 클라이언트 인증 실패")
	void apple_client_authentication_failure_error() {
		// given
		OAuthLoginRequest request = new OAuthLoginRequest("APPLE", null, "invalid-auth-code", null);

		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(400)
			.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.setBody("{\"error\": \"invalid_client\"}"));

		// when & then
		assertThatThrownBy(() -> oAuthService.oauthLogin(request))
			.isInstanceOf(OAuthException.class)
			.hasMessage(APPLE_AUTH_ERROR.getMessage());
	}

	@Test
	@DisplayName("클라이언트 시크릿 생성 성공")
	void generate_client_secret_success() throws Exception {
		// given
		KeyPairGenerator ecKeyGen = KeyPairGenerator.getInstance("EC");
		ecKeyGen.initialize(256);
		KeyPair ecPair = ecKeyGen.generateKeyPair();
		ECPrivateKey ecPrivateKey = (ECPrivateKey)ecPair.getPrivate();

		ReflectionTestUtils.setField(oAuthService, "privateKey",
			Base64.getEncoder().encodeToString(ecPrivateKey.getEncoded()));
		ReflectionTestUtils.setField(oAuthService, "teamId", "test-team-id");
		ReflectionTestUtils.setField(oAuthService, "clientId", "test-client-id");
		ReflectionTestUtils.setField(oAuthService, "keyId", "test-kid");
		ReflectionTestUtils.setField(oAuthService, "appleApiUrl", appleApiUrl);

		// when
		String clientSecret = oAuthService.generateClientSecret();

		// then
		assertThat(clientSecret).isNotNull();

		// JWT 구조 검증
		String[] parts = clientSecret.split("\\.");
		assertThat(parts).hasSize(3);  // header, payload, signature

		// header 검증
		String header = new String(Base64.getUrlDecoder().decode(parts[0]));
		assertThat(header).contains("\"kid\":\"test-kid\"");
	}

	@Test
	@DisplayName("Apple 로그인 실패 - Android")
	void apple_token_verification_failure_error_android() {
		// given
		OAuthLoginRequest request = new OAuthLoginRequest("APPLE", "asdf", null, null);

		mockWebServer.enqueue(new MockResponse().setResponseCode(401));

		// when & then
		assertThatThrownBy(() -> oAuthService.oauthLogin(request))
			.isInstanceOf(OAuthException.class)
			.hasMessage(APPLE_AUTH_ERROR.getMessage());
	}

	@Test
	@DisplayName("Apple 로그인 실패 - iOS")
	void apple_token_verification_failure_error_ios() {
		// given
		OAuthLoginRequest request = new OAuthLoginRequest("APPLE", null, "asdf", null);

		mockWebServer.enqueue(new MockResponse().setResponseCode(400));

		// when & then
		assertThatThrownBy(() -> oAuthService.oauthLogin(request))
			.isInstanceOf(OAuthException.class)
			.hasMessage(APPLE_AUTH_ERROR.getMessage());
	}

	@Test
	@DisplayName("Kakao 로그인 성공")
	void kakao_login_success() {
		// given
		OAuthLoginRequest request = new OAuthLoginRequest("KAKAO", "valid-token", null, null);
		Member member = mock(Member.class);
		given(member.getId()).willReturn(1L);

		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.setBody("{\"id\": \"12345\"}"));

		given(memberRepository.findBySocialIdAndSocialType("12345", SocialType.KAKAO)).willReturn(Optional.of(member));

		// when
		OAuthLoginResponse response = oAuthService.oauthLogin(request);

		// then
		assertThat(response).isNotNull();
		verify(sessionService).createSession(anyString(), eq("1"));
	}

	@Test
	@DisplayName("Apple 로그인 성공 - Android")
	void apple_login_success_android() throws Exception {
		// given
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(2048);
		KeyPair pair = keyGen.generateKeyPair();
		RSAPublicKey publicKey = (RSAPublicKey)pair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey)pair.getPrivate();

		String mockIdToken = JWT.create()
			.withKeyId("test-kid")
			.withIssuer(appleApiUrl)
			.withSubject("test-user-id")
			.withAudience("test-client-id")
			.withExpiresAt(new Date(System.currentTimeMillis() + 3600000))
			.withIssuedAt(new Date())
			.sign(Algorithm.RSA256(publicKey, privateKey));

		OAuthLoginRequest request = new OAuthLoginRequest("APPLE", null, null, mockIdToken);
		Member mockMember = mock(Member.class);
		given(mockMember.getId()).willReturn(1L);

		String mockN = Base64.getUrlEncoder().encodeToString(publicKey.getModulus().toByteArray());
		String mockE = Base64.getUrlEncoder().encodeToString(publicKey.getPublicExponent().toByteArray());

		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.setBody("{\"keys\": [{\"kid\": \"test-kid\", \"kty\": \"RSA\", \"n\": \"" + mockN + "\", \"e\": \"" + mockE
				+ "\"}]}"));

		given(memberRepository.findBySocialIdAndSocialType(anyString(), eq(SocialType.APPLE))).willReturn(
			Optional.of(mockMember));

		// when
		OAuthLoginResponse response = oAuthService.oauthLogin(request);

		// then
		assertThat(response).isNotNull();
		verify(sessionService).createSession(anyString(), eq("1"));
	}

	@Test
	@DisplayName("Apple 로그인 성공 - iOS")
	void apple_login_success_ios() throws Exception {
		// given
		KeyPairGenerator ecKeyGen = KeyPairGenerator.getInstance("EC");
		ecKeyGen.initialize(256);
		KeyPair ecPair = ecKeyGen.generateKeyPair();
		ECPrivateKey ecPrivateKey = (ECPrivateKey)ecPair.getPrivate();

		KeyPairGenerator rsaKeyGen = KeyPairGenerator.getInstance("RSA");
		rsaKeyGen.initialize(2048);
		KeyPair rsaPair = rsaKeyGen.generateKeyPair();
		RSAPublicKey rsaPublicKey = (RSAPublicKey)rsaPair.getPublic();
		RSAPrivateKey rsaPrivateKey = (RSAPrivateKey)rsaPair.getPrivate();
		String mockAuthCode = "valid_auth_code";

		// Apple 관련 필드값 설정
		ReflectionTestUtils.setField(oAuthService, "privateKey",
			Base64.getEncoder().encodeToString(ecPrivateKey.getEncoded()));
		ReflectionTestUtils.setField(oAuthService, "teamId", "test-team-id");
		ReflectionTestUtils.setField(oAuthService, "clientId", "test-client-id");
		ReflectionTestUtils.setField(oAuthService, "keyId", "test-key-id");
		ReflectionTestUtils.setField(oAuthService, "appleApiUrl", appleApiUrl);

		String mockIdToken = JWT.create()
			.withKeyId("test-kid")
			.withIssuer(appleApiUrl)
			.withSubject("test-user-id")
			.withAudience("test-client-id")
			.withExpiresAt(new Date(System.currentTimeMillis() + 3600000))
			.withIssuedAt(new Date())
			.sign(Algorithm.RSA256(rsaPublicKey, rsaPrivateKey));

		OAuthLoginRequest request = new OAuthLoginRequest("APPLE", null, mockAuthCode, null);
		Member mockMember = mock(Member.class);
		given(mockMember.getId()).willReturn(1L);

		String encodedPrivateKey = Base64.getEncoder().encodeToString(ecPrivateKey.getEncoded());
		ReflectionTestUtils.setField(oAuthService, "privateKey", encodedPrivateKey);

		OAuthService spyOAuthService = spy(oAuthService);
		doReturn("client_secret").when(spyOAuthService).generateClientSecret();

		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.setBody("{\"id_token\": \"" + mockIdToken + "\"}"));

		String mockN = Base64.getUrlEncoder().encodeToString(rsaPublicKey.getModulus().toByteArray());
		String mockE = Base64.getUrlEncoder().encodeToString(rsaPublicKey.getPublicExponent().toByteArray());

		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.setBody("{\"keys\": [{\"kid\": \"test-kid\", \"kty\": \"RSA\", \"n\": \"" + mockN + "\", \"e\": \"" + mockE
				+ "\"}]}"));

		given(memberRepository.findBySocialIdAndSocialType(anyString(), eq(SocialType.APPLE))).willReturn(
			Optional.of(mockMember));

		// when
		OAuthLoginResponse response = spyOAuthService.oauthLogin(request);

		// then
		assertThat(response).isNotNull();
		verify(sessionService).createSession(anyString(), eq("1"));
	}

	@Test
	@DisplayName("매칭되는 키가 없을 경우 예외 발생")
	void no_matching_key_error() {
		// given
		String mockIdToken = JWT.create()
			.withKeyId("test-kid")
			.withIssuer(appleApiUrl)
			.withSubject("test-user-id")
			.sign(Algorithm.HMAC256("secret"));

		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.setBody("{\"keys\": [{\"kid\": \"different-kid\"}]}"));

		OAuthLoginRequest request = new OAuthLoginRequest("APPLE", null, null, mockIdToken);

		// when & then
		assertThatThrownBy(() -> oAuthService.oauthLogin(request))
			.isInstanceOf(OAuthException.class)
			.hasMessage(APPLE_AUTH_ERROR.getMessage());
	}

	@Test
	@DisplayName("사용자 정보 추출 성공")
	void extract_user_info_success() {
		// given
		String payload = new JSONObject()
			.put("sub", "test-user")
			.put("email", "test@example.com")
			.toString();

		// when
		Map<String, Object> userInfo = oAuthService.extractUserInfo(payload);

		// then
		assertThat(userInfo)
			.containsEntry("sub", "test-user")
			.containsEntry("email", "test@example.com");
	}

	@Test
	@DisplayName("이메일이 없는 사용자 정보 추출 성공")
	void extract_user_info_without_email_success() {
		// given
		String payload = new JSONObject()
			.put("sub", "test-user")
			.toString();

		// when
		Map<String, Object> userInfo = oAuthService.extractUserInfo(payload);

		// then
		assertThat(userInfo)
			.containsEntry("sub", "test-user")
			.doesNotContainKey("email");
	}

	@Test
	@DisplayName("RSA 공개키 생성 실패 시 예외 발생")
	void get_rsa_public_key_error() {
		// given
		JSONObject invalidKey = new JSONObject()
			.put("n", "invalid")
			.put("e", "invalid");

		// when & then
		assertThatThrownBy(() -> oAuthService.getRsaPublicKey(invalidKey))
			.isInstanceOf(InvalidKeySpecException.class);
	}

	@Test
	@DisplayName("다양한 HTTP 상태코드에 따른 에러 처리")
	void handle_error_response_with_different_status() {
		// given
		OAuthLoginRequest request = new OAuthLoginRequest("KAKAO", "invalid-token", null, null);

		// 401 Unauthorized
		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(401)
			.setBody("{\"error\":\"invalid_token\"}"));

		// when & then
		assertThatThrownBy(() -> oAuthService.oauthLogin(request))
			.isInstanceOf(OAuthException.class)
			.hasMessage(FAILED_TO_FETCH_USER_INFO.getMessage());
	}

	@Test
	@DisplayName("Apple 공개키 요청 실패시 예외 발생")
	void fetch_apple_public_keys_error() {
		// given
		String validToken = JWT.create()
			.withKeyId("test-kid")
			.withIssuer(appleApiUrl)
			.withSubject("test-user")
			.sign(Algorithm.HMAC256("secret"));

		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(500)  // JWKS 엔드포인트 실패
			.setBody("Server Error"));

		OAuthLoginRequest request = new OAuthLoginRequest("APPLE", null, null, validToken);

		// when & then
		assertThatThrownBy(() -> oAuthService.oauthLogin(request))
			.isInstanceOf(OAuthException.class)
			.hasMessage(APPLE_AUTH_ERROR.getMessage());
	}

	@Test
	@DisplayName("잘못된 형식의 JWKS 응답일 경우 예외 발생")
	void invalid_jwks_format_error() {
		// given
		String validToken = JWT.create()
			.withKeyId("test-kid")
			.withIssuer(appleApiUrl)
			.withSubject("test-user")
			.sign(Algorithm.HMAC256("secret"));

		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setBody("invalid json"));  // 잘못된 JSON 형식

		OAuthLoginRequest request = new OAuthLoginRequest("APPLE", null, null, validToken);

		// when & then
		assertThatThrownBy(() -> oAuthService.oauthLogin(request))
			.isInstanceOf(OAuthException.class)
			.hasMessage(APPLE_AUTH_ERROR.getMessage());
	}
}
