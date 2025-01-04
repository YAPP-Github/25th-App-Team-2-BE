package com.tnt.global.auth;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

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
	private final SessionService sessionService;
	private final AntPathMatcher pathMatcher = new AntPathMatcher();
	private final List<String> allowedUris;

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

		String currentMemberSession = sessionService.extractCurrentMemberSession(request).orElse(null);

		log.info("사용자 세션 추출 - MemberSession: {}", currentMemberSession != null ? "존재" : "존재하지 않음");

		checkMemberSessionAndAuthentication(request, response, filterChain);
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

	public void checkMemberSessionAndAuthentication(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		log.info("checkMemberSessionAndAuthentication() 호출");
		sessionService.extractCurrentMemberSession(request)
			.filter(sessionService::validateCurrentMemberSession)
			.flatMap(sessionService::extractMemberId)
			.flatMap(memberId -> memberRepository.findByMemberIdAndMemberDelete(memberId, null))
			.ifPresent(this::saveAuthentication);

		filterChain.doFilter(request, response);
	}

	public void saveAuthentication(Member currentMember) {
		String password = currentMember.getEmail();

		UserDetails userDetailsUser = org.springframework.security.core.userdetails.User.builder()
			.username(currentMember.getMemberId())
			.password(password)
			.build();

		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetailsUser, null,
			authoritiesMapper.mapAuthorities(userDetailsUser.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}
