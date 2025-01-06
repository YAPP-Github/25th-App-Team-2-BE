package com.tnt.application.auth;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.tnt.domain.auth.SessionInfo;
import com.tnt.global.error.exception.UnauthorizedException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

	static final long SESSION_DURATION = 2L * 24 * 60 * 60; // 48시간
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";
	private final RedisTemplate<String, SessionInfo> redisTemplate;

	public String extractMemberSession(HttpServletRequest request) {
		String authHeader = request.getHeader(AUTHORIZATION_HEADER);
		if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
			log.info("Authorization 헤더가 존재하지 않거나 올바르지 않은 형식입니다.");
			throw new UnauthorizedException("인증 세션이 존재하지 않습니다.");
		}

		return authHeader.substring(BEARER_PREFIX.length());
	}

	public void validateMemberSession(String sessionId) {
		// 세션 존재 여부 확인
		if (Boolean.FALSE.equals(redisTemplate.hasKey(sessionId))) {
			log.info("세션이 존재하지 않음 - SessionId: {}", sessionId);
			throw new UnauthorizedException("세션 스토리지에 세션이 존재하지 않습니다.");
		}

		SessionInfo sessionInfo = redisTemplate.opsForValue().get(sessionId);

		// 세션 유효성 확인
		LocalDateTime lastAccessTime = sessionInfo.getLastAccessTime();
		if (lastAccessTime.isBefore(LocalDateTime.now().minusDays(2))) {
			log.info("세션이 만료됨 - SessionId: {}, LastAccessTime: {}", sessionId, lastAccessTime);
			redisTemplate.delete(sessionId);
			throw new UnauthorizedException("세션이 만료되었습니다.");
		}

		// 세션 갱신
		sessionInfo = SessionInfo.builder()
			.lastAccessTime(LocalDateTime.now())
			.userAgent(sessionInfo.getUserAgent())
			.clientIp(sessionInfo.getClientIp())
			.build();

		redisTemplate.opsForValue().set(sessionId, sessionInfo, SESSION_DURATION, TimeUnit.SECONDS);
		log.info("세션 유효성 검증 완료 및 갱신 - SessionId: {}", sessionId);
	}

	public void createSession(String memberId, HttpServletRequest request) {
		SessionInfo sessionInfo = SessionInfo.builder()
			.lastAccessTime(LocalDateTime.now())
			.userAgent(request.getHeader("User-Agent"))
			.clientIp(request.getRemoteAddr())
			.build();

		redisTemplate.opsForValue().set(
			memberId,
			sessionInfo,
			SESSION_DURATION,
			TimeUnit.SECONDS
		);
	}

	public void removeSession(String sessionId) {
		redisTemplate.delete(sessionId);
	}
}
