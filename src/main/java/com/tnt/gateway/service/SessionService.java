package com.tnt.gateway.service;

import static com.tnt.common.error.model.ErrorMessage.AUTHORIZATION_HEADER_ERROR;
import static com.tnt.common.error.model.ErrorMessage.NO_EXIST_SESSION_IN_STORAGE;
import static io.micrometer.common.util.StringUtils.isBlank;
import static java.util.Objects.isNull;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.tnt.common.error.exception.UnauthorizedException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionService {

	private static final long SESSION_DURATION = 7L * 24 * 60 * 60; // 24시간 * 7일
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String SESSION_ID_PREFIX = "SESSION-ID ";

	private final StringRedisTemplate redisTemplate;

	public String authenticate(HttpServletRequest request) {
		String authHeader = request.getHeader(AUTHORIZATION_HEADER);

		log.info("요청 인증 헤더 - AuthHeader: {}", authHeader);

		if (isBlank(authHeader) || !authHeader.startsWith(SESSION_ID_PREFIX)) {
			log.error("Authorization Header Error: [{}]", authHeader);
			throw new UnauthorizedException(AUTHORIZATION_HEADER_ERROR);
		}

		String sessionId = authHeader.substring(SESSION_ID_PREFIX.length());
		String sessionValue = redisTemplate.opsForValue().get(sessionId);

		if (isNull(sessionValue)) {
			throw new UnauthorizedException(NO_EXIST_SESSION_IN_STORAGE);
		}

		updateSession(sessionId, sessionValue);

		return sessionValue;
	}

	public void createSession(String sessionId, String memberId) {
		removeSession(memberId);
		redisTemplate.opsForValue().set(sessionId, memberId, SESSION_DURATION, TimeUnit.SECONDS);
		redisTemplate.opsForValue().set(memberId, sessionId, SESSION_DURATION, TimeUnit.SECONDS);
	}

	public void updateSession(String sessionId, String memberId) {
		redisTemplate.expire(sessionId, SESSION_DURATION, TimeUnit.SECONDS);
		redisTemplate.expire(memberId, SESSION_DURATION, TimeUnit.SECONDS);
	}

	public String removeSession(String dataKey) {
		String existingKey = redisTemplate.opsForValue().get(dataKey);

		if (!isNull(existingKey)) {
			redisTemplate.delete(existingKey);
		}

		redisTemplate.delete(dataKey);

		return existingKey;
	}
}
