package com.tnt.application.auth;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.tnt.domain.auth.SessionInfo;
import com.tnt.global.error.exception.UnauthorizedException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

	static final long SESSION_DURATION = 2L * 24 * 60 * 60; // 48시간
	private static final String SESSION_COOKIE_NAME = "SESSION";
	private final RedisTemplate<String, SessionInfo> redisTemplate;

	public Optional<String> extractMemberSession(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			log.info("쿠키가 존재하지 않습니다.");
			throw new UnauthorizedException("세션 쿠키가 존재하지 않습니다.");
		}

		return Arrays.stream(cookies)
			.filter(cookie -> SESSION_COOKIE_NAME.equals(cookie.getName()))
			.map(Cookie::getValue)
			.findFirst()
			.or(() -> {
				throw new UnauthorizedException("세션 쿠키가 존재하지 않습니다.");
			});
	}

	public void validateMemberSession(String sessionId) {
		// 1. 세션 존재 여부 확인
		if (Boolean.FALSE.equals(redisTemplate.hasKey(sessionId))) {
			log.info("세션이 존재하지 않음 - SessionId: {}", sessionId);
			throw new UnauthorizedException("세션 스토리지에 세션이 존재하지 않습니다.");
		}

		SessionInfo sessionInfo = redisTemplate.opsForValue().get(sessionId);

		// 2. 세션 유효성 확인
		LocalDateTime lastAccessTime = sessionInfo.getLastAccessTime();
		if (lastAccessTime.isBefore(LocalDateTime.now().minusDays(2))) {  // 48시간 지났는지 체크
			log.info("세션이 만료됨 - SessionId: {}, LastAccessTime: {}", sessionId, lastAccessTime);
			redisTemplate.delete(sessionId);  // 만료된 세션 삭제
			throw new UnauthorizedException("세션이 만료되었습니다.");
		}

		// 3. 세션 갱신 (마지막 접근 시간 업데이트)
		sessionInfo = SessionInfo.builder()
			.lastAccessTime(LocalDateTime.now())
			.userAgent(sessionInfo.getUserAgent())
			.clientIp(sessionInfo.getClientIp())
			.build();

		redisTemplate.opsForValue().set(sessionId, sessionInfo, SESSION_DURATION, TimeUnit.SECONDS);
		log.info("세션 유효성 검증 완료 및 갱신 - SessionId: {}", sessionId);
	}

	// 로그인 시 세션 생성을 위한 메서드
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

	// 로그아웃 시 세션 삭제
	public void removeSession(String sessionId) {
		redisTemplate.delete(sessionId);
	}
}
