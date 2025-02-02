package com.tnt.application.trainer;

import static com.tnt.common.error.model.ErrorMessage.TRAINER_NOT_FOUND;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tnt.common.error.exception.NotFoundException;
import com.tnt.domain.trainer.Trainer;
import com.tnt.dto.trainer.response.InvitationCodeResponse;
import com.tnt.dto.trainer.response.InvitationCodeVerifyResponse;
import com.tnt.infrastructure.mysql.repository.trainer.TrainerRepository;
import com.tnt.infrastructure.mysql.repository.trainer.TrainerSearchRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrainerService {

	private final TrainerRepository trainerRepository;
	private final TrainerSearchRepository trainerSearchRepository;

	public InvitationCodeResponse getInvitationCode(Long memberId) {
		Trainer trainer = getTrainerWithMemberId(memberId);

		return new InvitationCodeResponse(trainer.getInvitationCode());
	}

	public InvitationCodeVerifyResponse verifyInvitationCode(String invitationCode) {
		boolean isVerified = trainerRepository.findByInvitationCodeAndDeletedAtIsNull(invitationCode).isPresent();

		return new InvitationCodeVerifyResponse(isVerified);
	}

	@Transactional
	public InvitationCodeResponse reissueInvitationCode(Long memberId) {
		Trainer trainer = getTrainerWithMemberId(memberId);
		trainer.setNewInvitationCode();

		return new InvitationCodeResponse(trainer.getInvitationCode());
	}

	@Transactional
	public Trainer saveTrainer(Trainer trainer) {
		return trainerRepository.save(trainer);
	}

	public Trainer getTrainerWithMemberId(Long memberId) {
		return trainerRepository.findByMemberIdAndDeletedAtIsNull(memberId)
			.orElseThrow(() -> new NotFoundException(TRAINER_NOT_FOUND));
	}

	public Trainer getTrainerWithInvitationCode(String invitationCode) {
		return trainerSearchRepository.findByInvitationCode(invitationCode)
			.orElseThrow(() -> new NotFoundException(TRAINER_NOT_FOUND));
	}

}
