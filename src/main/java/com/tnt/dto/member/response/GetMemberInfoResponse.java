package com.tnt.dto.member.response;

import java.time.LocalDate;
import java.util.List;

import com.tnt.domain.member.MemberType;
import com.tnt.domain.member.SocialType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 조회 API 응답")
public record GetMemberInfoResponse(
	@Schema(description = "회원 이름", example = "홍길동", nullable = false)
	String name,

	@Schema(description = "이메일", example = "zxc098@kakao.com", nullable = false)
	String email,

	@Schema(description = "프로필 사진 URL", example = "https://images.tntapp.co.kr/profiles/trainers/basic_profile_trainer.svg", nullable = false)
	String profileImageUrl,

	@Schema(description = "회원 타입", example = "TRAINER", nullable = false)
	MemberType memberType,

	@Schema(description = "소셜 타입", example = "APPLE", nullable = false)
	SocialType socialType,

	@Schema(description = "트레이너 정보", nullable = true)
	TrainerInfo trainer,

	@Schema(description = "트레이니 정보", nullable = true)
	TraineeInfo trainee
) {

	public record TrainerInfo(
		@Schema(description = "관리 중인 회원", example = "23", nullable = true)
		Integer activeTraineeCount,

		@Schema(description = "함께했던 회원", example = "50", nullable = true)
		Integer totalTraineeCount,

		@Schema(description = "트레이너 초대 코드", example = "2H9DG4X3", nullable = true)
		String invitationCode
	) {

	}

	public record TraineeInfo(
		@Schema(description = "생년월일", example = "2025-01-01", nullable = true)
		LocalDate birthday,

		@Schema(description = "나이", example = "25", nullable = true)
		Integer age,

		@Schema(description = "키 (cm)", example = "180.5", nullable = true)
		Double height,

		@Schema(description = "몸무게 (kg)", example = "75.5", nullable = true)
		Double weight,

		@Schema(description = "주의사항", example = "가냘퍼요", nullable = true)
		String cautionNote,

		@Schema(description = "PT 목적들", example = "[\"체중 감량\", \"근력 향상\"]", nullable = false)
		List<String> ptGoals
	) {

	}
}
