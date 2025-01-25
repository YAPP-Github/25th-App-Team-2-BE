package com.tnt.application.trainee;

import static com.tnt.global.error.model.ErrorMessage.TRAINEE_NOT_FOUND;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tnt.domain.trainee.Trainee;
import com.tnt.global.error.exception.NotFoundException;
import com.tnt.infrastructure.mysql.repository.trainee.TraineeSearchRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TraineeService {

	private final TraineeSearchRepository traineeSearchRepository;

	public Trainee getTraineeWithMemberId(String memberId) {
		return traineeSearchRepository.findByMemberIdAndDeletedAtIsNull(Long.valueOf(memberId))
			.orElseThrow(() -> new NotFoundException(TRAINEE_NOT_FOUND));
	}

	public Trainee getTraineeWithId(String traineeId) {
		return traineeSearchRepository.findByIdAndDeletedAtIsNull(Long.valueOf(traineeId))
			.orElseThrow(() -> new NotFoundException(TRAINEE_NOT_FOUND));
	}
}
