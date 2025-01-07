package com.tnt.application.auth;

import static com.tnt.application.auth.SessionService.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.tnt.domain.auth.SessionValue;
import com.tnt.global.error.exception.UnauthorizedException;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

	@InjectMocks
	private SessionService sessionService;

	@Mock
	private RedisTemplate<String, SessionValue> redisTemplate;

	@Mock
	private ValueOperations<String, SessionValue> valueOperations;

	@Mock
	private HttpServletRequest request;

	@Test
	@DisplayName("요청 헤더에 세션이 없으면 예외 발생")
	void no_authorization_header_error() {
		// given
		given(request.getHeader("Authorization")).willReturn(null, " ");

		// when & then
		assertThatThrownBy(() -> sessionService.authenticate(request))
			.isInstanceOf(UnauthorizedException.class)
			.hasMessage("인가 세션이 존재하지 않습니다.");
	}

	@Test
	@DisplayName("Authorization 헤더가 SESSION-ID로 시작하지 않으면 예외 발생")
	void invalid_authorization_header_format_error() {
		// given
		given(request.getHeader("Authorization")).willReturn("Invalid 12345");

		// when & then
		assertThatThrownBy(() -> sessionService.authenticate(request))
			.isInstanceOf(UnauthorizedException.class)
			.hasMessage("인가 세션이 존재하지 않습니다.");
	}

	@Test
	@DisplayName("Authorization 헤더에서 세션 ID 추출 성공")
	void extract_session_id_success() {
		// given
		String sessionId = "test-session-id";
		SessionValue sessionValue = SessionValue.builder().build();

		given(request.getHeader("Authorization")).willReturn("SESSION-ID " + sessionId);
		given(redisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.get(sessionId)).willReturn(sessionValue);

		// when
		String extractedSessionId = sessionService.authenticate(request);

		// then
		assertThat(extractedSessionId).isEqualTo(sessionId);
	}

	@Test
	@DisplayName("세션 스토리지에 세션 존재하지 않으면 예외 발생")
	void session_does_not_exist_in_storage_error() {
		// given
		String sessionId = "test-session-id";

		given(request.getHeader("Authorization")).willReturn("SESSION-ID " + sessionId);
		given(redisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.get(sessionId)).willReturn(null);

		// when & then
		assertThatThrownBy(() -> sessionService.authenticate(request))
			.isInstanceOf(NullPointerException.class)
			.hasMessage("세션 스토리지에 세션이 존재하지 않습니다.");
	}

	@Test
	@DisplayName("세션 생성 성공")
	void create_session_success() {
		// given
		String memberId = "12345";

		given(redisTemplate.opsForValue()).willReturn(valueOperations);

		// when
		sessionService.createSession(memberId, request);

		// then
		verify(valueOperations).set(
			eq(memberId),
			any(SessionValue.class),
			eq(2L * 24 * 60 * 60),
			eq(TimeUnit.SECONDS)
		);
	}

	@Test
	@DisplayName("세션 스토리지에 저장 성공")
	void create_session_with_request_info_success() {
		// given
		String memberId = "12345";
		String userAgent = "Mozilla/5.0";
		String clientIp = "127.0.0.1";

		given(request.getHeader("User-Agent")).willReturn(userAgent);
		given(request.getRemoteAddr()).willReturn(clientIp);
		given(redisTemplate.opsForValue()).willReturn(valueOperations);

		// when
		sessionService.createSession(memberId, request);

		// then
		verify(valueOperations).set(
			eq(memberId),
			argThat(sessionValue ->
				sessionValue.getUserAgent().equals(userAgent) &&
					sessionValue.getClientIp().equals(clientIp) &&
					sessionValue.getLastAccessTime() != null
			),
			eq(SESSION_DURATION),
			eq(TimeUnit.SECONDS)
		);
	}

	@Test
	@DisplayName("세션 삭제 성공")
	void remove_session_success() {
		// given
		String sessionId = "12345";

		// when
		sessionService.removeSession(sessionId);

		// then
		verify(redisTemplate).delete(sessionId);
	}
}
