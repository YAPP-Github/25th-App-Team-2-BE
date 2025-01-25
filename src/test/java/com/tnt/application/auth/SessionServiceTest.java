package com.tnt.application.auth;

import static com.tnt.global.error.model.ErrorMessage.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.tnt.global.error.exception.UnauthorizedException;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

	@InjectMocks
	private SessionService sessionService;

	@Mock
	private StringRedisTemplate redisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

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
			.hasMessage(AUTHORIZATION_HEADER_ERROR.getMessage());
	}

	@Test
	@DisplayName("Authorization 헤더가 SESSION-ID로 시작하지 않으면 예외 발생")
	void invalid_authorization_header_format_error() {
		// given
		given(request.getHeader("Authorization")).willReturn("Invalid 12345");

		// when & then
		assertThatThrownBy(() -> sessionService.authenticate(request))
			.isInstanceOf(UnauthorizedException.class)
			.hasMessage(AUTHORIZATION_HEADER_ERROR.getMessage());
	}

	@Test
	@DisplayName("Authorization 헤더에서 세션 ID 추출 성공")
	void extract_session_id_success() {
		// given
		String sessionId = "test-session-id";
		String memberId = "12345";

		given(request.getHeader("Authorization")).willReturn("SESSION-ID " + sessionId);
		given(redisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.get(sessionId)).willReturn(memberId);

		// when
		String extractedSessionId = sessionService.authenticate(request);

		// then
		assertThat(extractedSessionId).isEqualTo(memberId);
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
			.isInstanceOf(UnauthorizedException.class)
			.hasMessage(NO_EXIST_SESSION_IN_STORAGE.getMessage());
	}

	@Test
	@DisplayName("세션 생성 성공")
	void create_session_success() {
		// given
		String sessionId = "test-session-id";
		String memberId = "12345";

		given(redisTemplate.opsForValue()).willReturn(valueOperations);

		// when
		sessionService.createSession(sessionId, memberId);

		// then
		verify(valueOperations).set(sessionId, memberId, 7L * 24 * 60 * 60, TimeUnit.SECONDS);
	}

	@Test
	@DisplayName("세션 삭제 성공")
	void remove_session_success() {
		// given
		String sessionId = "test-session-id";
		String memberId = "12345";

		given(redisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.get(sessionId)).willReturn(memberId);

		// when
		sessionService.removeSession(sessionId);

		// then
		verify(redisTemplate).delete(sessionId);
	}
}
