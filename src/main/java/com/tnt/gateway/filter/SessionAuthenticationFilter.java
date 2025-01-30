package com.tnt.gateway.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tnt.gateway.service.SessionService;

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
			filterChain.doFilter(request, response);
			return;
		}

		saveAuthentication(sessionService.authenticate(request));
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

		return allowed;
	}

	private void saveAuthentication(String memberId) {
		CustomUserDetails userDetails = new CustomUserDetails(Long.valueOf(memberId), memberId,
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
			userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(authentication);

		log.info("시큐리티 컨텍스트에 인증 정보 저장 완료 - MemberId: {}", memberId);
	}
}
