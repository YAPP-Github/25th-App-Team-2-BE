package com.tnt.global.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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

import com.tnt.application.auth.SessionService;
import com.tnt.global.error.exception.UnauthorizedException;

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

	private SessionAuthenticationFilter sessionAuthenticationFilter;

	@BeforeEach
	void setUp() {
		List<String> allowedUris = Arrays.asList("/api/auth/**", "/api/health");
		sessionAuthenticationFilter = new SessionAuthenticationFilter(
			allowedUris,
			sessionService
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
		verify(sessionService, never()).extractMemberSession(any());
		verify(filterChain).doFilter(request, response);
	}

	@Test
	@DisplayName("Authorization 헤더가 없는 경우 예외 발생")
	void missing_authorization_header_error() throws ServletException, IOException {
		// given
		given(request.getRequestURI()).willReturn("/api/members/me");
		given(sessionService.extractMemberSession(request))
			.willThrow(new UnauthorizedException("인증 세션이 존재하지 않습니다."));

		StringWriter stringWriter = new StringWriter();
		given(response.getWriter()).willReturn(new PrintWriter(stringWriter));

		// when
		sessionAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// then
		verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		verify(response).setContentType("application/json;charset=UTF-8");
		verify(filterChain, never()).doFilter(request, response);
		assertThat(stringWriter.toString()).contains("인증 세션이 존재하지 않습니다.");
	}

	@Test
	@DisplayName("세션이 스토리지에 존재하지 않는 경우 예외 발생")
	void session_not_exist_in_storage_error() throws ServletException, IOException {
		// given
		String sessionId = "12345";
		given(request.getRequestURI()).willReturn("/api/members/me");
		given(sessionService.extractMemberSession(request)).willReturn(sessionId);
		willThrow(new UnauthorizedException("세션 스토리지에 세션이 존재하지 않습니다."))
			.given(sessionService)
			.validateMemberSession(sessionId);

		StringWriter stringWriter = new StringWriter();
		given(response.getWriter()).willReturn(new PrintWriter(stringWriter));

		// when
		sessionAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// then
		verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		verify(response).setContentType("application/json;charset=UTF-8");
		verify(filterChain, never()).doFilter(request, response);
		assertThat(stringWriter.toString()).contains("세션 스토리지에 세션이 존재하지 않습니다.");
	}

	@Test
	@DisplayName("유효한 세션이 아닐 경우 예외 발생")
	void expired_session_error() throws ServletException, IOException {
		// given
		String sessionId = "12345";
		given(request.getRequestURI()).willReturn("/api/members/me");
		given(sessionService.extractMemberSession(request)).willReturn(sessionId);
		willThrow(new UnauthorizedException("세션이 만료되었습니다."))
			.given(sessionService)
			.validateMemberSession(sessionId);

		StringWriter stringWriter = new StringWriter();
		given(response.getWriter()).willReturn(new PrintWriter(stringWriter));

		// when
		sessionAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// then
		verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		verify(response).setContentType("application/json;charset=UTF-8");
		verify(filterChain, never()).doFilter(request, response);
		assertThat(stringWriter.toString()).contains("세션이 만료되었습니다.");
	}

	@Test
	@DisplayName("유효한 세션으로 인증 성공")
	void authenticate_with_valid_session_success() throws ServletException, IOException {
		// given
		String sessionId = "12345";
		given(request.getRequestURI()).willReturn("/api/members/me");
		given(sessionService.extractMemberSession(request)).willReturn(sessionId);
		willDoNothing().given(sessionService).validateMemberSession(sessionId);

		// when
		sessionAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// then
		verify(filterChain).doFilter(request, response);
		verify(sessionService).extractMemberSession(request);
		verify(sessionService).validateMemberSession(sessionId);
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}
}
