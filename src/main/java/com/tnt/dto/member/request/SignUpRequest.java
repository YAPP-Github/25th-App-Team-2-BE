package com.tnt.dto.member.request;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

@Schema(description = "회원가입 API 요청")
public record SignUpRequest(
	@Schema(description = "FCM 토큰", example = "dsl5f7iho-28yg2g290u2fj0-23348-23r05", nullable = false)
	@NotBlank(message = "FCM 토큰은 필수입니다.")
	String fcmToken,

	@Schema(description = "회원 타입 (trainer / trainee)", example = "trainer", nullable = false)
	@Pattern(regexp = "^(trainer|trainee)$", message = "회원 타입은 trainer 또는 trainee만 가능합니다.")
	String memberType,

	@Schema(description = "소셜 로그인 타입 (KAKAO / APPLE)", example = "KAKAO", allowableValues = {"KAKAO",
		"APPLE"}, nullable = false)
	@Pattern(regexp = "^(KAKAO|APPLE)$", message = "소셜 로그인 타입은 KAKAO 또는 APPLE만 가능합니다.")
	String socialType,

	@Schema(description = "소셜 ID", example = "563931436", nullable = false)
	@NotBlank(message = "소셜 ID는 필수입니다.")
	String socialId,

	@Schema(description = "소셜 이메일", example = "zxc098@kakao.com", nullable = false)
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	String socialEmail,

	@Schema(description = "서비스 이용 약관 동의 여부", example = "true", nullable = false)
	@NotNull(message = "서비스 이용 약관 동의 여부는 필수입니다.")
	Boolean serviceAgreement,

	@Schema(description = "개인 정보 수집 동의 여부", example = "true", nullable = false)
	@NotNull(message = "개인 정보 수집 동의 여부는 필수입니다.")
	Boolean collectionAgreement,

	@Schema(description = "광고성 알림 수신 동의 여부", example = "true", nullable = false)
	@NotNull(message = "광고성 알림 수신 동의 여부는 필수입니다.")
	Boolean advertisementAgreement,

	@Schema(description = "회원 이름", example = "홍길동", nullable = false)
	@NotBlank(message = "회원 이름은 필수입니다.")
	String name,

	@Schema(description = "생년월일", example = "2025-01-01", nullable = true)
	@Past(message = "생년월일은 과거 날짜여야 합니다.")
	LocalDate birthday,

	@Schema(description = "키 (cm)", example = "180.5", nullable = true)
	@Digits(integer = 3, fraction = 2, message = "키는 정수부 3자리, 소수점 2자리까지 입력 가능합니다.")
	Double height,

	@Schema(description = "몸무게 (kg)", example = "75.5", nullable = true)
	@Digits(integer = 3, fraction = 2, message = "몸무게는 정수부 3자리, 소수점 2자리까지 입력 가능합니다.")
	Double weight,

	@Schema(description = "주의사항", example = "가냘퍼요", nullable = true)
	String cautionNote,

	@Schema(description = "PT 목적들", example = "[\"체중 감량\", \"근력 향상\"]", nullable = true)
	List<String> goalContents
) {

}
