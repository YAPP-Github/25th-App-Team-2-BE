package com.tnt.application.trainee;

import static com.tnt.common.error.model.ErrorMessage.TRAINEE_NOT_FOUND;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tnt.common.error.exception.NotFoundException;
import com.tnt.domain.trainee.Trainee;
import com.tnt.infrastructure.mysql.repository.trainee.TraineeRepository;
import com.tnt.infrastructure.mysql.repository.trainee.TraineeSearchRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TraineeService {

	private final TraineeRepository traineeRepository;
	private final TraineeSearchRepository traineeSearchRepository;

	public Trainee getTraineeWithMemberId(String memberId) {
		return traineeSearchRepository.findByMemberIdAndDeletedAtIsNull(Long.valueOf(memberId))
			.orElseThrow(() -> new NotFoundException(TRAINEE_NOT_FOUND));
	}

	public Trainee getTraineeWithId(String traineeId) {
		return traineeSearchRepository.findByIdAndDeletedAtIsNull(Long.valueOf(traineeId))
			.orElseThrow(() -> new NotFoundException(TRAINEE_NOT_FOUND));
	}

	public Trainee saveTrainee(Trainee trainee) {
		return traineeRepository.save(trainee);
	}

	public void softDeleteTrainee(Trainee trainee) {
		LocalDateTime now = LocalDateTime.now();

		trainee.updateDeletedAt(now);
	}
}
