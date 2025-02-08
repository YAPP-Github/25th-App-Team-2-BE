package com.tnt.gateway.filter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willThrow;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tnt.gateway.service.SessionService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class SessionAuthenticationFilterTest {

	@Mock
	private SessionService sessionService;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private FilterChain filterChain;

	@Mock
	private ObjectMapper objectMapper;

	private SessionAuthenticationFilter sessionAuthenticationFilter;

	@BeforeEach
	void setUp() {
		List<String> allowedUris = Arrays.asList("/api/auth/**", "/api/health");
		sessionAuthenticationFilter = new SessionAuthenticationFilter(
			allowedUris,
			sessionService,
			objectMapper
		);
	}

	@Test
	@DisplayName("허용된 URI 세션 검증 스킵 성공")
	void allowed_uri_skip_session_verification_success() throws ServletException, IOException {
		// given
		given(request.getRequestURI()).willReturn("/api/auth/login");
		given(request.getQueryString()).willReturn(null);

		// when
		sessionAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// then
		verify(sessionService, never()).authenticate(any());
		verify(filterChain).doFilter(request, response);
	}

	@Test
	@DisplayName("유효한 세션으로 인증 성공")
	void authenticate_with_valid_session_success() throws ServletException, IOException {
		// given
		String sessionId = "12345";

		given(request.getRequestURI()).willReturn("/api/members/me");
		given(sessionService.authenticate(request.getHeader("Authorization"))).willReturn(sessionId);

		// when
		sessionAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// then
		verify(filterChain).doFilter(request, response);
		verify(sessionService).authenticate(request.getHeader("Authorization"));
	}

	@Test
	@DisplayName("필터 체인 실행 중 ServletException 예외 발생")
	void handle_servlet_exception_error() throws ServletException, IOException {
		// given
		String sessionId = "12345";

		given(request.getRequestURI()).willReturn("/api/members/me");
		given(sessionService.authenticate(request.getHeader("Authorization"))).willReturn(sessionId);
		willThrow(new ServletException("필터 체인 에러"))
			.given(filterChain)
			.doFilter(any(), any());

		// when & then
		assertThatThrownBy(() ->
			sessionAuthenticationFilter.doFilterInternal(request, response, filterChain))
			.isInstanceOf(ServletException.class)
			.hasMessage("필터 체인 에러");
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}
}
