package com.tnt.application.trainer;

import static com.tnt.domain.member.MemberType.TRAINER;
import static com.tnt.domain.trainer.Trainer.INVITATION_CODE_LENGTH;
import static com.tnt.domain.trainer.Trainer.builder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tnt.common.error.exception.NotFoundException;
import com.tnt.domain.member.Member;
import com.tnt.domain.member.SocialType;
import com.tnt.domain.trainer.Trainer;
import com.tnt.dto.trainer.response.InvitationCodeResponse;
import com.tnt.dto.trainer.response.InvitationCodeVerifyResponse;
import com.tnt.infrastructure.mysql.repository.trainer.TrainerRepository;
import com.tnt.infrastructure.mysql.repository.trainer.TrainerSearchRepository;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

	@InjectMocks
	private TrainerService trainerService;

	@Mock
	private TrainerRepository trainerRepository;

	@Mock
	private TrainerSearchRepository trainerSearchRepository;

	@Test
	@DisplayName("트레이너 초대 코드 불러오기 성공")
	void get_invitation_code_success() {
		// given
		Long trainerId = 1L;
		Long memberId = 1L;
		String socialId = "1234567890";
		String email = "abc@gmail.com";
		String name = "김영명";
		String profileImageUrl = "https://profile.com/1234567890";

		Member member = Member.builder()
			.id(memberId)
			.socialId(socialId)
			.email(email)
			.name(name)
			.profileImageUrl(profileImageUrl)
			.serviceAgreement(true)
			.collectionAgreement(true)
			.advertisementAgreement(true)
			.socialType(SocialType.KAKAO)
			.memberType(TRAINER)
			.build();

		Trainer trainer = builder()
			.id(trainerId)
			.member(member)
			.build();

		given(trainerRepository.findByMemberIdAndDeletedAtIsNull(memberId)).willReturn(
			java.util.Optional.of(trainer));

		// when
		InvitationCodeResponse response = trainerService.getInvitationCode(memberId);

		// then
		assertThat(response.invitationCode()).isNotNull();
		assertThat(response.invitationCode()).hasSize(INVITATION_CODE_LENGTH);
	}

	@Test
	@DisplayName("트레이너 초대 코드 불러오기 실패 - 존재하지 않는 계정")
	void get_invitation_code_no_member_fail() {
		// given
		Long memberId = 99L;

		given(trainerRepository.findByMemberIdAndDeletedAtIsNull(memberId)).willReturn(java.util.Optional.empty());

		// when & then
		assertThatThrownBy(() -> trainerService.getInvitationCode(memberId)).isInstanceOf(
			NotFoundException.class);
	}

	@Test
	@DisplayName("트레이너 초대 코드 재발급 성공")
	void reissue_invitation_code_success() {
		// given
		Long trainerId = 1L;
		Long memberId = 30L;
		String socialId = "1234567890";
		String email = "abc@gmail.com";
		String name = "김영명";
		String profileImageUrl = "https://profile.com/1234567890";

		Member member = Member.builder()
			.id(memberId)
			.socialId(socialId)
			.email(email)
			.name(name)
			.profileImageUrl(profileImageUrl)
			.serviceAgreement(true)
			.collectionAgreement(true)
			.advertisementAgreement(true)
			.socialType(SocialType.KAKAO)
			.memberType(TRAINER)
			.build();

		Trainer trainer = Trainer.builder()
			.id(trainerId)
			.member(member)
			.build();

		String invitationCodeBefore = trainer.getInvitationCode();

		given(trainerRepository.findByMemberIdAndDeletedAtIsNull(memberId)).willReturn(
			java.util.Optional.of(trainer));

		// when
		InvitationCodeResponse response = trainerService.reissueInvitationCode(memberId);

		// then
		assertThat(response.invitationCode()).isNotNull();
		assertThat(response.invitationCode()).hasSize(INVITATION_CODE_LENGTH);
		assertThat(response.invitationCode()).isNotEqualTo(invitationCodeBefore);
	}

	@Test
	@DisplayName("트레이너 초대 코드 인증 성공")
	void verify_invitation_code_success() {
		// given
		String code = "2H9DG4X3";

		Long memberId = 30L;
		String socialId = "1234567890";
		String email = "abc@gmail.com";
		String name = "김영명";
		String profileImageUrl = "https://profile.com/1234567890";

		Member member = Member.builder()
			.id(memberId)
			.socialId(socialId)
			.email(email)
			.name(name)
			.profileImageUrl(profileImageUrl)
			.serviceAgreement(true)
			.collectionAgreement(true)
			.advertisementAgreement(true)
			.socialType(SocialType.KAKAO)
			.memberType(TRAINER)
			.build();

		given(trainerRepository.existsByInvitationCodeAndDeletedAtIsNull(code))
			.willReturn(true);
		given(trainerSearchRepository.findByInvitationCode(code))
			.willReturn(java.util.Optional.of(Trainer.builder().member(member).build()));

		// when
		InvitationCodeVerifyResponse response = trainerService.verifyInvitationCode(code);

		// then
		assertThat(response.isVerified()).isTrue();
	}
}
