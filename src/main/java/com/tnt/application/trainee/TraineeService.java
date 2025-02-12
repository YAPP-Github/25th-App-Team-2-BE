package com.tnt.application.trainee;

import static com.tnt.common.error.model.ErrorMessage.TRAINEE_NOT_FOUND;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tnt.common.error.exception.NotFoundException;
import com.tnt.domain.trainee.Trainee;
import com.tnt.infrastructure.mysql.repository.trainee.TraineeRepository;
import com.tnt.infrastructure.mysql.repository.trainee.TraineeSearchRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TraineeService {

	private final TraineeRepository traineeRepository;
	private final TraineeSearchRepository traineeSearchRepository;

	@Transactional
	public Trainee saveTrainee(Trainee trainee) {
		return traineeRepository.save(trainee);
	}

	public Trainee getTraineeWithMemberId(Long memberId) {
		return traineeSearchRepository.findByMemberId(memberId)
			.orElseThrow(() -> new NotFoundException(TRAINEE_NOT_FOUND));
	}

	public Trainee getTraineeWithMemberIdNoFetch(Long memberId) {
		return traineeRepository.findByMemberIdAndDeletedAtIsNull(memberId)
			.orElseThrow(() -> new NotFoundException(TRAINEE_NOT_FOUND));
	}

	public Trainee getTraineeWithId(Long traineeId) {
		return traineeSearchRepository.findById(traineeId)
			.orElseThrow(() -> new NotFoundException(TRAINEE_NOT_FOUND));
	}
}
