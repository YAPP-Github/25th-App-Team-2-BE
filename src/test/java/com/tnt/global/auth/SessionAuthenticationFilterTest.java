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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

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
		verify(sessionService, never()).authenticate(any());
		verify(filterChain).doFilter(request, response);
	}

	@ParameterizedTest
	@DisplayName("세션 인증 실패 시 예외 발생")
	@ValueSource(strings = {
		"인가 세션이 존재하지 않습니다.",
		"세션 스토리지에 세션이 존재하지 않습니다.",
		"세션이 만료되었습니다."
	})
	void session_authentication_failure_cases(String errorMessage) throws ServletException, IOException {
		// given
		StringWriter stringWriter = new StringWriter();

		given(request.getRequestURI()).willReturn("/api/members/me");
		if (errorMessage.equals("세션 스토리지에 세션이 존재하지 않습니다.")) {
			given(sessionService.authenticate(request)).willThrow(new RuntimeException(errorMessage));
		} else {
			given(sessionService.authenticate(request)).willThrow(new UnauthorizedException(errorMessage));
		}
		given(response.getWriter()).willReturn(new PrintWriter(stringWriter));

		// when
		sessionAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// then
		verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		verify(response).setContentType("application/json;charset=UTF-8");
		verify(filterChain, never()).doFilter(request, response);
		assertThat(stringWriter.toString()).contains(errorMessage);
	}

	@Test
	@DisplayName("유효한 세션으로 인증 정보 저장 성공")
	void save_authentication_success() throws ServletException, IOException {
		// given
		String sessionId = "12345";

		given(request.getRequestURI()).willReturn("/api/members/me");
		given(sessionService.authenticate(request)).willReturn(sessionId);

		// when
		sessionAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// then
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		assertThat(authentication).isNotNull();
		assertThat(authentication.getPrincipal()).isInstanceOf(UserDetails.class);
		UserDetails userDetails = (UserDetails)authentication.getPrincipal();
		assertThat(userDetails.getUsername()).isEqualTo(sessionId);
		assertThat(userDetails.getAuthorities())
			.extracting("authority")
			.contains("ROLE_USER");
	}

	@Test
	@DisplayName("유효한 세션으로 인증 성공")
	void authenticate_with_valid_session_success() throws ServletException, IOException {
		// given
		String sessionId = "12345";

		given(request.getRequestURI()).willReturn("/api/members/me");
		given(sessionService.authenticate(request)).willReturn(sessionId);

		// when
		sessionAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// then
		verify(filterChain).doFilter(request, response);
		verify(sessionService).authenticate(request);
	}

	@Test
	@DisplayName("필터 체인 실행 중 ServletException 예외 발생")
	void handle_servlet_exception_error() throws ServletException, IOException {
		// given
		String sessionId = "12345";

		given(request.getRequestURI()).willReturn("/api/members/me");
		given(sessionService.authenticate(request)).willReturn(sessionId);
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
