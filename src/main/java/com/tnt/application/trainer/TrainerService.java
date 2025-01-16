package com.tnt.application.trainer;

import static com.tnt.global.error.model.ErrorMessage.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tnt.domain.trainer.Trainer;
import com.tnt.dto.trainer.response.InvitationCodeResponse;
import com.tnt.global.error.exception.NotFoundException;
import com.tnt.infrastructure.mysql.repository.trainer.TrainerRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TrainerService {

	private final TrainerRepository trainerRepository;

	public InvitationCodeResponse getInvitationCode(String memberId) {
		Trainer trainer = getTrainer(memberId);

		return new InvitationCodeResponse(String.valueOf(trainer.getId()), trainer.getInvitationCode());
	}

	@Transactional
	public InvitationCodeResponse reissueInvitationCode(String memberId) {
		Trainer trainer = getTrainer(memberId);
		trainer.setNewInvitationCode();

		return new InvitationCodeResponse(String.valueOf(trainer.getId()), trainer.getInvitationCode());
	}

	public Trainer getTrainer(String memberId) {
		return trainerRepository.findByMemberIdAndDeletedAtIsNull(Long.valueOf(memberId))
			.orElseThrow(() -> new NotFoundException(TRAINER_NOT_FOUND));
	}
}
