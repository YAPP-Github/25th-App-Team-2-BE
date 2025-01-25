package com.tnt.application.auth;

import static com.tnt.global.error.model.ErrorMessage.*;
import static io.micrometer.common.util.StringUtils.*;
import static java.util.Objects.*;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.tnt.global.error.exception.UnauthorizedException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SessionService {

	private static final long SESSION_DURATION = 7L * 24 * 60 * 60; // 24시간 * 7일
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String SESSION_ID_PREFIX = "SESSION-ID ";

	private final StringRedisTemplate redisTemplate;

	public String authenticate(HttpServletRequest request) {
		String authHeader = request.getHeader(AUTHORIZATION_HEADER);

		if (isBlank(authHeader) || !authHeader.startsWith(SESSION_ID_PREFIX)) {
			throw new UnauthorizedException(AUTHORIZATION_HEADER_ERROR);
		}

		String sessionId = authHeader.substring(SESSION_ID_PREFIX.length());
		String sessionValue = redisTemplate.opsForValue().get(sessionId);

		if (isNull(sessionValue)) {
			throw new UnauthorizedException(NO_EXIST_SESSION_IN_STORAGE);
		}

		createOrUpdateSession(sessionId, sessionValue);

		return sessionValue;
	}

	public void createOrUpdateSession(String sessionId, String memberId) {
		removeSession(memberId);
		redisTemplate.opsForValue().set(sessionId, memberId, SESSION_DURATION, TimeUnit.SECONDS);
		redisTemplate.opsForValue().set(memberId, sessionId, SESSION_DURATION, TimeUnit.SECONDS);
	}

	public String removeSession(String dataKey) {
		String existingKey = redisTemplate.opsForValue().get(dataKey);

		if (nonNull(existingKey)) {
			redisTemplate.delete(existingKey);
		}

		redisTemplate.delete(dataKey);

		return existingKey;
	}
}
