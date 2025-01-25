package com.tnt.application.trainee;

import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tnt.global.error.exception.NotFoundException;
import com.tnt.infrastructure.mysql.repository.trainee.TraineeSearchRepository;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

	@InjectMocks
	private TraineeService traineeService;

	@Mock
	private TraineeSearchRepository traineeSearchRepository;

	@Test
	void getTraineeWithMemberId_success() {
		// given
		String memberId = "1234567";

		given(traineeSearchRepository.findByMemberIdAndDeletedAtIsNull(Long.valueOf(memberId))).willReturn(
			Optional.empty());

		// when & then
		Assertions.assertThatThrownBy(() -> traineeService.getTraineeWithMemberId(memberId))
			.isInstanceOf(NotFoundException.class);
	}
}
