package com.tnt.dto.member.request;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@Schema(description = "회원가입 API 요청")
public record SignUpRequest(
	@Schema(description = "회원 타입 (trainer / trainee)", example = "trainer")
	@NotBlank(message = "회원 타입은 필수입니다.")
	String memberType,

	@Schema(description = "소셜 로그인 타입 (KAKAO / APPLE)", example = "KAKAO", allowableValues = {"KAKAO", "APPLE"})
	@NotBlank(message = "소셜 로그인 타입은 필수입니다.")
	String socialType,

	@Schema(description = "소셜 ID", example = "563931436")
	@NotBlank(message = "소셜 ID는 필수입니다.")
	String socialId,

	@Schema(description = "소셜 이메일", example = "zxc098@kakao.com")
	@NotBlank(message = "소셜 이메일은 필수입니다.")
	String socialEmail,

	@Schema(description = "회원 이름", example = "홍길동")
	@NotBlank(message = "회원 이름은 필수입니다.")
	String name,

	@Schema(description = "프로필 사진 URL", example = "asdf.png")
	String profileImageUrl,

	@Schema(description = "생년월일", example = "2025-01-01")
	LocalDate birthday,

	@Schema(description = "키", example = "300")
	Double height,

	@Schema(description = "몸무게", example = "255.5")
	Double weight,

	@Schema(description = "주의사항", example = "가냘퍼요")
	String cautionNote,

	@Schema(description = "PT 목적들", example = "[\"체중 감량\", \"근력 향상\"]")
	@NotEmpty(message = "PT 목적은 필수입니다.")
	List<String> goalContents
) {

}
