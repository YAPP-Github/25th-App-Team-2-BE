package com.tnt.global.auth;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tnt.application.auth.SessionService;
import com.tnt.global.error.exception.UnauthorizedException;

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
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		String requestUri = request.getRequestURI();
		String queryString = request.getQueryString();
		log.info("들어온 요청 - URI: {}, Query: {}, Method: {}",
			requestUri,
			queryString != null ? queryString : "쿼리 스트링 없음",
			request.getMethod());

		if (isAllowedUri(requestUri)) {
			log.info("{} 허용 URI. 세션 유효성 검사 스킵.", requestUri);
			filterChain.doFilter(request, response);
			return;
		}

		try {
			checkSessionAndAuthentication(request, response, filterChain);
		} catch (UnauthorizedException e) {
			handleUnauthorizedException(response, e);
		}
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

	private void checkSessionAndAuthentication(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		log.info("세션 검증 시작");

		String sessionId = sessionService.extractMemberSession(request)
			.orElseThrow(() -> new UnauthorizedException("세션이 존재하지 않습니다."));

		sessionService.validateMemberSession(sessionId);

		Long memberId = Long.parseLong(sessionId);

		saveAuthentication(memberId);
		filterChain.doFilter(request, response);
	}

	private void handleUnauthorizedException(
		HttpServletResponse response,
		UnauthorizedException exception
	) throws IOException {
		log.error("인증 실패: {}", exception.getMessage());
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json;charset=UTF-8");

		Map<String, Object> errorResponse = Map.of(
			"message", exception.getMessage(),
			"status", HttpServletResponse.SC_UNAUTHORIZED,
			"timestamp", LocalDateTime.now().toString()
		);

		new ObjectMapper().writeValueAsString(errorResponse);
		response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
	}

	private void saveAuthentication(Long memberId) {
		UserDetails userDetails = User.builder()
			.username(String.valueOf(memberId))
			.password("")
			.roles("USER")
			.build();

		Authentication authentication = new UsernamePasswordAuthenticationToken(
			userDetails,
			null,
			authoritiesMapper.mapAuthorities(userDetails.getAuthorities())
		);

		SecurityContextHolder.getContext().setAuthentication(authentication);
		log.info("시큐리티 컨텍스트에 인증 정보 저장 완료 - MemberId: {}", memberId);
	}
}
