package com.tnt.application.trainer;

import static com.tnt.domain.trainer.Trainer.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tnt.domain.trainer.Trainer;
import com.tnt.dto.trainer.response.InvitationCodeResponse;
import com.tnt.global.error.exception.NotFoundException;
import com.tnt.infrastructure.mysql.repository.trainer.TrainerRepository;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

	@InjectMocks
	private TrainerService trainerService;

	@Mock
	private TrainerRepository trainerRepository;

	@Test
	@DisplayName("트레이너 초대 코드 불러오기 성공")
	void get_invitation_code_success() {
		// given
		Long trainerId = 1L;
		Long memberId = 1L;

		Trainer trainer = Trainer.builder()
			.id(trainerId)
			.memberId(memberId)
			.build();

		given(trainerRepository.findByMemberIdAndDeletedAtIsNull(memberId)).willReturn(
			java.util.Optional.of(trainer));

		// when
		InvitationCodeResponse response = trainerService.getInvitationCode(String.valueOf(memberId));

		// then
		assertThat(response.trainerId()).isEqualTo(String.valueOf(trainerId));
		assertThat(response.invitationCode()).isNotNull();
		assertThat(response.invitationCode()).hasSize(INVITATION_CODE_LENGTH);
	}

	@Test
	@DisplayName("트레이너 초대 코드 불러오기 실패 - 존재하지 않는 계정")
	void get_invitation_code_no_member_fail() {
		// given
		Long memberId = 99L;
		String memberIdString = String.valueOf(memberId);

		given(trainerRepository.findByMemberIdAndDeletedAtIsNull(memberId)).willReturn(java.util.Optional.empty());

		// when & then
		assertThatThrownBy(() -> trainerService.getInvitationCode(memberIdString)).isInstanceOf(
			NotFoundException.class);
	}

	@Test
	@DisplayName("트레이너 초대 코드 재발급 성공")
	void reissue_invitation_code_success() {
		// given
		Long trainerId = 1L;
		Long memberId = 123L;

		Trainer trainer = Trainer.builder()
			.id(trainerId)
			.memberId(memberId)
			.build();

		String invitationCodeBefore = trainer.getInvitationCode();

		given(trainerRepository.findByMemberIdAndDeletedAtIsNull(memberId)).willReturn(
			java.util.Optional.of(trainer));

		// when
		InvitationCodeResponse response = trainerService.reissueInvitationCode(String.valueOf(memberId));

		// then
		assertThat(response.trainerId()).isEqualTo(String.valueOf(trainerId));
		assertThat(response.invitationCode()).isNotNull();
		assertThat(response.invitationCode()).hasSize(INVITATION_CODE_LENGTH);
		assertThat(response.invitationCode()).isNotEqualTo(invitationCodeBefore);
	}
}
