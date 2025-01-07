package com.tnt.global.auth;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tnt.application.auth.SessionService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SessionAuthenticationFilter extends OncePerRequestFilter {

	private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();
	private final AntPathMatcher pathMatcher = new AntPathMatcher();
	private final List<String> allowedUris;
	private final SessionService sessionService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		String requestUri = request.getRequestURI();
		String queryString = request.getQueryString();

		log.info("들어온 요청 - URI: {}, Query: {}, Method: {}", requestUri, queryString != null ? queryString : "쿼리 스트링 없음",
			request.getMethod());
		if (isAllowedUri(requestUri)) {
			log.info("{} 허용 URI. 세션 유효성 검사 스킵.", requestUri);
			filterChain.doFilter(request, response);
			return;
		}

		try {
			checkSessionAndAuthentication(request);
		} catch (RuntimeException e) {
			log.error("인증 처리 중 에러 발생: ", e);
			handleUnauthorizedException(response, e);
		}

		filterChain.doFilter(request, response);
	}

	private boolean isAllowedUri(String requestUri) {
		boolean allowed = false;

		for (String pattern : allowedUris) {
			if (pathMatcher.match(pattern, requestUri)) {
				allowed = true;
				break;
			}
		}
		log.info("URI {} is {}allowed", requestUri, allowed ? "" : "not ");

		return allowed;
	}

	private void checkSessionAndAuthentication(HttpServletRequest request) {
		String sessionId = sessionService.authenticate(request);

		saveAuthentication(Long.parseLong(sessionId));
	}

	private void handleUnauthorizedException(HttpServletResponse response, RuntimeException exception) throws
		IOException {
		log.error("인증 실패: ", exception);
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write(exception.getMessage());
	}

	private void saveAuthentication(Long sessionId) {
		UserDetails userDetails = User.builder()
			.username(String.valueOf(sessionId))
			.password("")
			.roles("USER")
			.build();

		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
			authoritiesMapper.mapAuthorities(userDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		log.info("시큐리티 컨텍스트에 인증 정보 저장 완료 - SessionId: {}", sessionId);
	}
}
