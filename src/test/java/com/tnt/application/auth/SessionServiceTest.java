package com.tnt.application.auth;

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
	@DisplayName("요청에 세션 쿠키가 없으면 예외 발생")
	void request_does_not_have_session_cookie_error() {
		// given
		given(request.getCookies()).willReturn(null);

		// when
		assertThatThrownBy(() -> sessionService.extractMemberSession(request))
			.isInstanceOf(UnauthorizedException.class)
			.hasMessage("세션 쿠키가 존재하지 않습니다.");
	}

	@Test
	@DisplayName("세션 스토리지에 세션 존재하지 않으면 예외 발생")
	void session_does_not_exist_in_storage_error() {
		// given
		String sessionId = "test-session-id";
		given(redisTemplate.hasKey(sessionId)).willReturn(false);

		// when & then
		assertThatThrownBy(() -> sessionService.validateMemberSession(sessionId))
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

		given(redisTemplate.hasKey(sessionId)).willReturn(true);
		given(redisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.get(sessionId)).willReturn(sessionInfo);

		// when & then
		assertThatThrownBy(() -> sessionService.validateMemberSession(sessionId))
			.isInstanceOf(UnauthorizedException.class)
			.hasMessage("세션이 만료되었습니다.");

		verify(redisTemplate).delete(sessionId);
	}

	@Test
	@DisplayName("세션 생성 성공")
	void create_session_success() {
		// given
		given(redisTemplate.opsForValue()).willReturn(valueOperations);
		String memberId = "12345";

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
}
