package com.tnt.application.trainee;

import static com.tnt.common.error.model.ErrorMessage.TRAINEE_NOT_FOUND;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tnt.common.error.exception.NotFoundException;
import com.tnt.domain.trainee.Trainee;
import com.tnt.infrastructure.mysql.repository.trainee.TraineeSearchRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TraineeService {

	private final TraineeSearchRepository traineeSearchRepository;

	public Trainee getTraineeWithMemberId(Long memberId) {
		return traineeSearchRepository.findByMemberIdAndDeletedAtIsNull(memberId)
			.orElseThrow(() -> new NotFoundException(TRAINEE_NOT_FOUND));
	}

	public Trainee getTraineeWithId(Long traineeId) {
		return traineeSearchRepository.findByIdAndDeletedAtIsNull(traineeId)
			.orElseThrow(() -> new NotFoundException(TRAINEE_NOT_FOUND));
	}
}
