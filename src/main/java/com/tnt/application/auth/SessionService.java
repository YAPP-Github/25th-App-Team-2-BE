package com.tnt.application.auth;

import static com.tnt.global.error.model.ErrorMessage.*;
import static io.micrometer.common.util.StringUtils.*;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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

	private final StringRedisTemplate redisTemplate;

	public String authenticate(HttpServletRequest request) {
		String authHeader = request.getHeader(AUTHORIZATION_HEADER);

		if (isBlank(authHeader) || !authHeader.startsWith(SESSION_ID_PREFIX)) {
			log.error(AUTHORIZATION_HEADER_ERROR.getMessage());

			throw new UnauthorizedException(AUTHORIZATION_HEADER_ERROR);
		}

		String sessionId = authHeader.substring(SESSION_ID_PREFIX.length());
		String sessionValue = redisTemplate.opsForValue().get(sessionId);

		if (sessionValue == null) {
			log.error(NO_EXIST_SESSION_IN_STORAGE.getMessage());

			throw new UnauthorizedException(NO_EXIST_SESSION_IN_STORAGE);
		}

		createOrUpdateSession(sessionId, "");

		return sessionValue;
	}

	public void createOrUpdateSession(String sessionId, String memberId) {
		if (isBlank(memberId)) { // 세션 갱신
			redisTemplate.expire(sessionId, SESSION_DURATION, TimeUnit.SECONDS);
			redisTemplate.expire(memberId, SESSION_DURATION, TimeUnit.SECONDS);
		} else { // 로그인 시 기존 로그인 상태 제거하고 새로운 세션 생성
			String existingSessionId = redisTemplate.opsForValue().get(memberId);

			if (existingSessionId != null) {
				removeSession(sessionId);
				removeSession(memberId);
			}
			redisTemplate.opsForValue().set(sessionId, memberId, SESSION_DURATION, TimeUnit.SECONDS);
			redisTemplate.opsForValue().set(memberId, sessionId, SESSION_DURATION, TimeUnit.SECONDS);
		}
	}

	public void removeSession(String dataKey) {
		redisTemplate.delete(dataKey);
	}
}
