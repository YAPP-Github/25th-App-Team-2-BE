package com.tnt.common.error.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {

	SERVER_ERROR("서버 에러가 발생했습니다."),
	FCM_FAILED("FCM 전송에 실패했습니다."),
	IMAGE_PROCESSING_ERROR("이미지 처리 중 오류가 발생했습니다."),
	IMAGE_LOAD_ERROR("이미지를 읽을 수 없습니다."),
	S3_UPLOAD_ERROR("S3로 이미지 업로드 중 오류가 발생했습니다."),
	S3_DELETE_ERROR("S3 이미지 삭제 중 오류가 발생했습니다."),
	IMAGE_NOT_FOUND("이미지가 존재하지 않습니다."),
	IMAGE_NOT_SUPPORT("지원하지 않는 이미지 형식입니다. jpg, jpeg, png, svg만 가능합니다."),
	INVALID_REQUEST_NOT_MATCH("요청자와 요청이 일치하지 않습니다."),
	INVALID_REQUEST_BODY("요청 바디가 올바르지 않습니다."),

	CLIENT_BAD_REQUEST("잘못된 요청입니다."),
	FAILED_TO_PROCESS_REQUEST("요청 진행에 실패했습니다."),
	ACCESS_DENIED("접근이 거부되었습니다."),
	MISSING_REQUIRED_PARAMETER_ERROR("필수 파라미터 '%s'가 누락되었습니다."),
	PARAMETER_FORMAT_NOT_CORRECT("파라미터 '%s'의 형식이 올바르지 않습니다."),
	INPUT_VALUE_IS_INVALID("입력값이 유효하지 않습니다."),
	REQUEST_BODY_EMPTY("request body가 비어있습니다."),
	INVALID_FORMAT_DATETIME("DateTime 형식이 잘못되었습니다."),

	AUTHORIZATION_HEADER_ERROR("Authorization 헤더가 존재하지 않거나 올바르지 않은 형식입니다."),
	UNAUTHORIZED("인증되지 않은 사용자입니다."),
	NO_EXIST_SESSION_IN_STORAGE("세션 스토리지에 세션이 존재하지 않습니다."),

	UNSUPPORTED_SOCIAL_TYPE("지원하지 않는 소셜 서비스입니다."),
	KAKAO_SERVER_ERROR("Kakao 서버 에러가 발생했습니다."),
	APPLE_SERVER_ERROR("Apple 서버 에러가 발생했습니다."),
	APPLE_AUTH_ERROR("Apple 인증에 실패했습니다."),
	FAILED_TO_FETCH_PRIVATE_KEY("시크릿 키 불러오기에 실패했습니다."),
	FAILED_TO_CREATE_APPLE_CLIENT_SECRET("애플 클라이언트 시크릿 생성 중 에러가 발생했습니다."),
	MATCHING_KEY_NOT_FOUND("매칭키 찾기에 실패했습니다."),
	FAILED_TO_VERIFY_ID_TOKEN("Apple ID 토큰 검증에 실패했습니다."),

	MEMBER_NOT_FOUND("존재하지 않는 회원입니다."),
	MEMBER_FCM_TOKEN_NOT_FOUND("회원 FCM 토큰이 존재하지 않습니다."),
	MEMBER_CONFLICT("이미 존재하는 회원입니다."),
	MEMBER_INVALID_SOCIAL_ID("유효하지 않는 소셜 ID 입니다."),
	MEMBER_INVALID_EMAIL("유효하지 않는 EMAIL 입니다."),
	MEMBER_INVALID_NAME("유효하지 않는 이름입니다."),
	MEMBER_INVALID_PROFILE_IMAGE_URL("유효하지 않는 프로필입니다."),
	MEMBER_INVALID_SOCIAL_TYPE("유효하지 않는 소셜 타입입니다."),
	UNSUPPORTED_MEMBER_TYPE("지원하지 않는 회원 타입입니다."),
	MEMBER_INVALID_SERVICE_AGREEMENT("서비스 이용 약관 동의 여부는 true 여야 합니다."),
	MEMBER_INVALID_COLLECTION_AGREEMENT("개인 정보 수집 동의 여부는 true 여야 합니다."),
	MEMBER_NULL_ADVERTISEMENT_AGREEMENT("회원 광고성 알림 수신 동의 여부가 null 입니다."),
	MEMBER_NULL_PUSH_AGREEMENT("회원 푸쉬 알림 수신 동의 여부가 null 입니다."),

	TRAINER_NULL("트레이너가 null 입니다."),
	TRAINER_NULL_MEMBER("트레이너 member 가 null 입니다."),
	TRAINER_INVALID_INVITATION_CODE("초대 코드가 올바르지 않습니다."),
	TRAINER_NOT_FOUND("존재하지 않는 트레이너입니다."),
	TRAINER_INVITATION_CODE_GENERATE_FAILED("트레이너 초대 코드 생성에 실패했습니다."),

	TRAINEE_NULL("트레이니가 null 입니다."),
	TRAINEE_NULL_MEMBER("트레이니 member 가 null 입니다."),
	TRAINEE_NULL_HEIGHT("트레이니 height가 null 입니다."),
	TRAINEE_NULL_WEIGHT("트레이니 weight가 null 입니다."),
	TRAINEE_INVALID_CAUTION_NOTE("주의사항이 올바르지 않습니다."),
	TRAINEE_NOT_FOUND("존재하지 않는 트레이니입니다."),

	PT_GOAL_NULL_TRAINEE_ID("PT 목적 트레이니 id가 null 입니다."),
	PT_GOAL_INVALID_CONTENT("목적 내용이 올바르지 않습니다."),

	PT_TRAINER_TRAINEE_ALREADY_EXIST("이미 연결된 트레이너-트레이니입니다."),
	PT_TRAINEE_ALREADY_EXIST("이미 다른 트레이너와 연결되어 있습니다."),
	PT_TRAINER_TRAINEE_NOT_FOUND("존재하지 않는 연결 정보입니다."),
	PT_TRAINER_TRAINEE_NULL("트레이너 - 트레이니 연결 정보가 null 입니다."),

	PT_LESSON_INVALID_MEMO("수업 메모의 길이는 공백 포함 30자 이하이어야 합니다."),
	PT_LESSON_DUPLICATE_TIME("이미 예약된 시간대입니다."),
	PT_LESSON_NOT_FOUND("존재하지 않는 수업입니다."),
	PT_LESSON_OVERFLOW("총 수업 수 보다 수업을 더 추가할 수 없습니다."),
	PT_LESSON_MORE_THAN_ONE_A_DAY("한명의 수업은 하루에 하나만 추가할 수 있습니다."),

	DIET_NULL_TRAINEE_ID("식단 트레이니 id가 null 입니다."),
	DIET_INVALID_IMAGE_URL("유효하지 않는 식단 사진입니다."),
	DIET_INVALID_MEMO("식단 메모가 올바르지 않습니다."),
	UNSUPPORTED_DIET_TYPE("지원하지 않는 식단 타입입니다."),
	DIET_NOT_FOUND("존재하지 않는 식단입니다.");

	private final String message;
}
