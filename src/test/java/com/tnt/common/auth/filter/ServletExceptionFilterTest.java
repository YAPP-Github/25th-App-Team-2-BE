package com.tnt.common.auth.filter;

import static com.tnt.common.error.model.ErrorMessage.ACCESS_DENIED;
import static com.tnt.common.error.model.ErrorMessage.AUTHORIZATION_HEADER_ERROR;
import static com.tnt.common.error.model.ErrorMessage.CLIENT_BAD_REQUEST;
import static com.tnt.common.error.model.ErrorMessage.SERVER_ERROR;
import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.tnt.common.error.exception.UnauthorizedException;
import com.tnt.gateway.filter.ServletExceptionFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class ServletExceptionFilterTest {

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private FilterChain filterChain;

	private ServletExceptionFilter servletExceptionFilter;
	private StringWriter stringWriter;

	@BeforeEach
	void setUp() throws IOException {
		servletExceptionFilter = new ServletExceptionFilter();
		stringWriter = new StringWriter();
		given(response.getWriter()).willReturn(new PrintWriter(stringWriter));
	}

	@Test
	@DisplayName("IllegalArgumentException 발생 시 400 응답 성공")
	void handle_illegal_argument_exception_success() throws ServletException, IOException {
		// given
		doThrow(new IllegalArgumentException(CLIENT_BAD_REQUEST.getMessage()))
			.when(filterChain)
			.doFilter(request, response);

		// when
		servletExceptionFilter.doFilterInternal(request, response, filterChain);

		// then
		verify(response).setStatus(SC_BAD_REQUEST);
		verify(response).setContentType("application/json;charset=UTF-8");
		assertThat(stringWriter.toString()).hasToString(CLIENT_BAD_REQUEST.getMessage());
	}

	@Test
	@DisplayName("UnauthorizedException 발생 시 401 응답 성공")
	void handle_unauthorized_exception_success() throws ServletException, IOException {
		// given
		doThrow(new UnauthorizedException(AUTHORIZATION_HEADER_ERROR))
			.when(filterChain)
			.doFilter(request, response);

		// when
		servletExceptionFilter.doFilterInternal(request, response, filterChain);

		// then
		verify(response).setStatus(SC_UNAUTHORIZED);
		verify(response).setContentType("application/json;charset=UTF-8");
		assertThat(stringWriter.toString()).hasToString(AUTHORIZATION_HEADER_ERROR.getMessage());
	}

	@Test
	@DisplayName("AccessDeniedException 발생 시 403 응답 성공")
	void handle_access_denied_exception_success() throws ServletException, IOException {
		// given
		doThrow(new AccessDeniedException(ACCESS_DENIED.getMessage()))
			.when(filterChain)
			.doFilter(request, response);

		// when
		servletExceptionFilter.doFilterInternal(request, response, filterChain);

		// then
		verify(response).setStatus(SC_FORBIDDEN);
		verify(response).setContentType("application/json;charset=UTF-8");
		assertThat(stringWriter.toString()).hasToString(ACCESS_DENIED.getMessage());
	}

	@Test
	@DisplayName("기타 RuntimeException 발생 시 500 응답 성공")
	void handle_other_runtime_exception_success() throws ServletException, IOException {
		// given
		doThrow(new RuntimeException(SERVER_ERROR.getMessage()))
			.when(filterChain)
			.doFilter(request, response);

		// when
		servletExceptionFilter.doFilterInternal(request, response, filterChain);

		// then
		verify(response).setStatus(SC_INTERNAL_SERVER_ERROR);
		verify(response).setContentType("application/json;charset=UTF-8");
		assertThat(stringWriter.toString()).hasToString(SERVER_ERROR.getMessage());
	}
}
