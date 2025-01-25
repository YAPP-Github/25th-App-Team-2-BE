package com.tnt.application.trainer;

import static com.tnt.global.error.model.ErrorMessage.TRAINER_NOT_FOUND;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tnt.domain.trainer.Trainer;
import com.tnt.dto.trainer.response.InvitationCodeResponse;
import com.tnt.dto.trainer.response.InvitationCodeVerifyResponse;
import com.tnt.global.error.exception.NotFoundException;
import com.tnt.infrastructure.mysql.repository.trainer.TrainerRepository;
import com.tnt.infrastructure.mysql.repository.trainer.TrainerSearchRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TrainerService {

	private final TrainerRepository trainerRepository;
	private final TrainerSearchRepository trainerSearchRepository;

	public InvitationCodeResponse getInvitationCode(String memberId) {
		Trainer trainer = getTrainerWithMemberId(memberId);

		return new InvitationCodeResponse(trainer.getInvitationCode());
	}

	public InvitationCodeVerifyResponse verifyInvitationCode(String invitationCode) {
		boolean isVerified = trainerRepository.findByInvitationCodeAndDeletedAtIsNull(invitationCode).isPresent();

		return new InvitationCodeVerifyResponse(isVerified);
	}

	@Transactional
	public InvitationCodeResponse reissueInvitationCode(String memberId) {
		Trainer trainer = getTrainerWithMemberId(memberId);
		trainer.setNewInvitationCode();

		return new InvitationCodeResponse(trainer.getInvitationCode());
	}

	public Trainer getTrainerWithMemberId(String memberId) {
		return trainerRepository.findByMemberIdAndDeletedAtIsNull(Long.valueOf(memberId))
			.orElseThrow(() -> new NotFoundException(TRAINER_NOT_FOUND));
	}

	public Trainer getTrainerWithInvitationCode(String invitationCode) {
		return trainerSearchRepository.findByInvitationCodeAndDeletedAtIsNull(invitationCode)
			.orElseThrow(() -> new NotFoundException(TRAINER_NOT_FOUND));
	}
}
