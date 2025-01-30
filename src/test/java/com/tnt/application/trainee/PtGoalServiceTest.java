package com.tnt.application.trainee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tnt.domain.trainee.PtGoal;
import com.tnt.infrastructure.mysql.repository.trainee.PtGoalRepository;

@ExtendWith(MockitoExtension.class)
class PtGoalServiceTest {

	@Mock
	private PtGoalRepository ptGoalRepository;

	@InjectMocks
	private PtGoalService ptGoalService;

	@Test
	@DisplayName("traineeId로 PT 목표 목록 조회 성공")
	void get_all_pt_goals_with_trainee_id_success() {
		// given
		Long traineeId = 1L;
		List<PtGoal> ptGoals = List.of(
			PtGoal.builder()
				.traineeId(traineeId)
				.content("목표1")
				.build(),
			PtGoal.builder()
				.traineeId(traineeId)
				.content("목표2")
				.build()
		);

		given(ptGoalRepository.findAllByTraineeIdAndDeletedAtIsNull(traineeId))
			.willReturn(ptGoals);

		// when
		List<PtGoal> result = ptGoalService.getAllPtGoalsWithTraineeId(traineeId);

		// then
		assertThat(result).isNotNull().hasSize(2).isEqualTo(ptGoals);
		verify(ptGoalRepository).findAllByTraineeIdAndDeletedAtIsNull(traineeId);
	}

	@Test
	@DisplayName("PT 목표 목록 저장 성공")
	void save_all_pt_goals_success() {
		// given
		List<PtGoal> ptGoals = List.of(
			PtGoal.builder()
				.traineeId(1L)
				.content("목표1")
				.build(),
			PtGoal.builder()
				.traineeId(1L)
				.content("목표2")
				.build()
		);

		given(ptGoalRepository.saveAll(ptGoals)).willReturn(ptGoals);

		// when
		List<PtGoal> savedPtGoals = ptGoalService.saveAllPtGoals(ptGoals);

		// then
		assertThat(savedPtGoals).isNotNull().hasSize(2).isEqualTo(ptGoals);
		verify(ptGoalRepository).saveAll(ptGoals);
	}

	@Test
	@DisplayName("PT 목표 목록 soft delete 성공")
	void soft_delete_all_pt_goals_success() {
		// given
		List<PtGoal> ptGoals = List.of(
			PtGoal.builder()
				.traineeId(1L)
				.content("목표1")
				.build(),
			PtGoal.builder()
				.traineeId(1L)
				.content("목표2")
				.build()
		);

		// when
		ptGoalService.softDeleteAllPtGoals(ptGoals);

		// then
		ptGoals.forEach(ptGoal -> assertThat(ptGoal.getDeletedAt()).isNotNull());
	}
}
