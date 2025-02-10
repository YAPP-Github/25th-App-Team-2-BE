package com.tnt.domain.pt;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tnt.domain.member.Member;
import com.tnt.domain.trainee.Trainee;
import com.tnt.domain.trainer.Trainer;
import com.tnt.fixture.MemberFixture;
import com.tnt.fixture.PtTrainerTraineeFixture;
import com.tnt.fixture.TraineeFixture;
import com.tnt.fixture.TrainerFixture;

class PtLessonTest {

	@Test
	@DisplayName("길이가 30을 넘은 메모 생성 실패")
	void create_memo_over_length_fail() {
		// given
		Long trainerId = 1L;
		Long traineeId = 2L;

		String failMemo = "123456789012345678901234567890149238749823479823479734239874";

		Member trainerMember = MemberFixture.getTrainerMember1();
		Member traineeMember = MemberFixture.getTraineeMember1();

		Trainer trainer = TrainerFixture.getTrainer1(trainerId, trainerMember);
		Trainee trainee = TraineeFixture.getTrainee1WithId(traineeId, traineeMember);

		PtTrainerTrainee ptTrainerTrainee = PtTrainerTraineeFixture.getPtTrainerTrainee1(trainer, trainee);

		//when & then
		assertThatThrownBy(() -> PtLesson.builder()
			.ptTrainerTrainee(ptTrainerTrainee)
			.lessonStart(LocalDateTime.of(2021, 1, 1, 10, 0))
			.lessonEnd(LocalDateTime.of(2021, 1, 1, 11, 0))
			.memo(failMemo)
			.build()
		).isInstanceOf(IllegalArgumentException.class);
	}
}
