package com.tnt.global.auth.filter;

import static com.tnt.global.error.model.ErrorMessage.*;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tnt.global.error.exception.UnauthorizedException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServletExceptionFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		try {
			filterChain.doFilter(request, response);
		} catch (RuntimeException e) {
			log.error(FAILED_TO_PROCESS_REQUEST.getMessage(), e);

			handleException(response, e);
		}
	}

	private void handleException(HttpServletResponse response, RuntimeException exception) throws IOException {
		response.setContentType("application/json;charset=UTF-8");

		if (exception instanceof UnauthorizedException) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		} else if (exception instanceof AccessDeniedException) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		} else if (exception instanceof IllegalArgumentException) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} else {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		response.getWriter().write(exception.getMessage());
	}
}
