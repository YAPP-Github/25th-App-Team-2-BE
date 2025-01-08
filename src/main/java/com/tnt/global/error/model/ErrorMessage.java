package com.tnt.global.error.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {

	AUTHORIZATION_HEADER_ERROR("Authorization 헤더가 존재하지 않거나 올바르지 않은 형식입니다."),

	SOCIAL_SERVICE_NOT_SUPPORT("지원하지 않는 소셜 서비스입니다."),
	FAILED_TO_FETCH_USER_INFO("소셜 서버로부터 유저 정보 불러오기에 실패했습니다."),
	APPLE_AUTH_ERROR("Apple 인증에 실패했습니다."),
	FAILED_TO_FETCH_PRIVATE_KEY("시크릿 키 불러오기에 실패했습니다."),
	FAILED_TO_CREATE_APPLE_CLIENT_SECRET("애플 클라이언트 시크릿 생성 중 에러가 발생했습니다."),
	MATCHING_KEY_NOT_FOUND("매칭키 찾기에 실패했습니다."),
	FAILED_TO_VERIFY_ID_TOKEN("Apple ID 토큰 검증에 실패했습니다."),

	MEMBER_NOT_FOUND("존재하지 않는 회원입니다.");

	private final String message;
}
