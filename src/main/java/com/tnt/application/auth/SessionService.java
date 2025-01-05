package com.tnt.application.auth;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.tnt.domain.auth.SessionInfo;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

	private static final String SESSION_COOKIE_NAME = "SESSION";
	private static final String LOGIN_STATUS = "login";
	private static final long SESSION_DURATION = 2 * 24 * 60 * 60L; // 48시간
	private final RedisTemplate<String, SessionInfo> redisTemplate;

	public Optional<String> extractMemberSession(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			log.info("쿠키가 존재하지 않습니다.");
			return Optional.empty();
		}

		return Arrays.stream(cookies)
			.filter(cookie -> SESSION_COOKIE_NAME.equals(cookie.getName()))
			.map(Cookie::getValue)
			.findFirst();
	}

	public boolean validateMemberSession(String sessionId) {
		SessionInfo sessionInfo = redisTemplate.opsForValue().get(sessionId);
		boolean isValid = LOGIN_STATUS.equals(sessionInfo.getStatus());

		log.info("세션 검증 결과 - SessionId: {}, Valid: {}", sessionId, isValid);
		return isValid;
	}

	public Optional<Long> extractMemberId(String sessionId) {
		try {
			return Optional.of(Long.parseLong(sessionId.split(":")[0]));
		} catch (Exception e) {
			log.error("세션 ID에서 회원 ID를 추출하는데 실패했습니다. sessionId: {}", sessionId, e);
			return Optional.empty();
		}
	}

	// 로그인 시 세션 생성을 위한 메서드
	public void createSession(String memberId, HttpServletRequest request) {
		SessionInfo sessionInfo = SessionInfo.builder()
			.status(LOGIN_STATUS)
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

	// 로그아웃 시 세션 삭제를 위한 메서드
	public void removeSession(String sessionId) {
		redisTemplate.delete(sessionId);
	}
}
