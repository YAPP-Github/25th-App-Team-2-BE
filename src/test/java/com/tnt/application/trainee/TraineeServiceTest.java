package com.tnt.application.trainee;

import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tnt.common.error.exception.NotFoundException;
import com.tnt.infrastructure.mysql.repository.trainee.TraineeSearchRepository;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

	@InjectMocks
	private TraineeService traineeService;

	@Mock
	private TraineeSearchRepository traineeSearchRepository;

	@Test
	void getTraineeWithMemberId_fail() {
		// given
		Long memberId = 1234567L;

		given(traineeSearchRepository.findByMemberId(memberId)).willReturn(
			Optional.empty());

		// when & then
		Assertions.assertThatThrownBy(() -> traineeService.getTraineeWithMemberId(memberId))
			.isInstanceOf(NotFoundException.class);
	}

	@Test
	void getTraineeWithId_fail() {
		// given
		Long traineeId = 124124L;

		given(traineeSearchRepository.findById(traineeId)).willReturn(Optional.empty());

		// when & then
		Assertions.assertThatThrownBy(() -> traineeService.getTraineeWithId(traineeId))
			.isInstanceOf(NotFoundException.class);
	}
}
