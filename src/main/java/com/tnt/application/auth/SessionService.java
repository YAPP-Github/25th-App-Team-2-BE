package com.tnt.application.auth;

import static io.micrometer.common.util.StringUtils.*;
import static java.util.Objects.*;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.tnt.domain.auth.SessionValue;
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
	private static final String SESSION_ID_PREFIX = "SESSION-ID ";
	private final RedisTemplate<String, SessionValue> redisTemplate;

	public String authenticate(HttpServletRequest request) {
		String authHeader = request.getHeader(AUTHORIZATION_HEADER);

		if (isBlank(authHeader) || !authHeader.startsWith(SESSION_ID_PREFIX)) {
			log.error("Authorization 헤더가 존재하지 않거나 올바르지 않은 형식입니다.");

			throw new UnauthorizedException("인가 세션이 존재하지 않습니다.");
		}

		String sessionId = authHeader.substring(SESSION_ID_PREFIX.length());

		requireNonNull(redisTemplate.opsForValue().get(sessionId), "세션 스토리지에 세션이 존재하지 않습니다.");

		return sessionId;
	}

	public void createSession(String memberId, HttpServletRequest request) {
		SessionValue sessionValue = SessionValue.builder()
			.lastAccessTime(LocalDateTime.now())
			.userAgent(request.getHeader("User-Agent"))
			.clientIp(request.getRemoteAddr())
			.build();

		redisTemplate.opsForValue().set(
			memberId,
			sessionValue,
			SESSION_DURATION,
			TimeUnit.SECONDS
		);
	}

	public void removeSession(String sessionId) {
		redisTemplate.delete(sessionId);
	}
}
