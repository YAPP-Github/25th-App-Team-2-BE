package com.tnt.application.auth;

import static com.tnt.application.auth.SessionService.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.tnt.domain.auth.SessionInfo;
import com.tnt.global.error.exception.UnauthorizedException;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

	@InjectMocks
	private SessionService sessionService;

	@Mock
	private RedisTemplate<String, SessionInfo> redisTemplate;

	@Mock
	private ValueOperations<String, SessionInfo> valueOperations;

	@Mock
	private HttpServletRequest request;

	@Test
	@DisplayName("요청 헤더에 세션이 없으면 예외 발생")
	void no_authorization_header_error() {
		// given
		given(request.getHeader("Authorization")).willReturn(null);

		// when & then
		assertThatThrownBy(() -> sessionService.authenticate(request))
			.isInstanceOf(UnauthorizedException.class)
			.hasMessage("인증 세션이 존재하지 않습니다.");
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

		given(request.getHeader("Authorization")).willReturn("SESSION-ID " + sessionId);

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
		given(redisTemplate.hasKey(sessionId)).willReturn(false);

		// when & then
		assertThatThrownBy(() -> sessionService.authenticate(request))
			.isInstanceOf(UnauthorizedException.class)
			.hasMessage("세션 스토리지에 세션이 존재하지 않습니다.");
	}

	@Test
	@DisplayName("세션이 만료되면 예외 발생")
	void session_expires_error() {
		// given
		String sessionId = "test-session-id";
		SessionInfo sessionInfo = SessionInfo.builder()
			.lastAccessTime(LocalDateTime.now().minusDays(3)) // 48시간 초과
			.build();

		given(request.getHeader("Authorization")).willReturn("SESSION-ID " + sessionId);
		given(redisTemplate.hasKey(sessionId)).willReturn(true);
		given(redisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.get(sessionId)).willReturn(sessionInfo);

		// when & then
		assertThatThrownBy(() -> sessionService.authenticate(request))
			.isInstanceOf(UnauthorizedException.class)
			.hasMessage("세션이 만료되었습니다.");

		verify(redisTemplate).delete(sessionId);
	}

	@Test
	@DisplayName("세션 유효성 검증 및 갱신 성공")
	void validate_and_refresh_session_success() {
		// given
		String sessionId = "test-session-id";
		SessionInfo sessionInfo = SessionInfo.builder()
			.lastAccessTime(LocalDateTime.now().minusHours(1))
			.userAgent("Mozilla")
			.clientIp("127.0.0.1")
			.build();

		given(request.getHeader("Authorization")).willReturn("SESSION-ID " + sessionId);
		given(redisTemplate.hasKey(sessionId)).willReturn(true);
		given(redisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.get(sessionId)).willReturn(sessionInfo);

		// when
		sessionService.authenticate(request);

		// then
		verify(valueOperations).set(
			eq(sessionId),
			any(SessionInfo.class),
			eq(SESSION_DURATION),
			eq(TimeUnit.SECONDS)
		);
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
			any(SessionInfo.class),
			eq(2L * 24 * 60 * 60),
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
