package com.tnt.application.pt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tnt.application.trainee.TraineeService;
import com.tnt.application.trainer.TrainerService;
import com.tnt.common.error.exception.ConflictException;
import com.tnt.domain.member.Member;
import com.tnt.domain.pt.PtLesson;
import com.tnt.domain.pt.PtTrainerTrainee;
import com.tnt.domain.trainee.Trainee;
import com.tnt.domain.trainer.Trainer;
import com.tnt.dto.trainer.ConnectWithTrainerDto;
import com.tnt.dto.trainer.request.ConnectWithTrainerRequest;
import com.tnt.dto.trainer.response.GetPtLessonsOnDateResponse;
import com.tnt.fixture.MemberFixture;
import com.tnt.fixture.PtTrainerTraineeFixture;
import com.tnt.fixture.TraineeFixture;
import com.tnt.fixture.TrainerFixture;
import com.tnt.infrastructure.mysql.repository.pt.PtLessonSearchRepository;
import com.tnt.infrastructure.mysql.repository.pt.PtTrainerTraineeRepository;

@ExtendWith(MockitoExtension.class)
class PtServiceTest {

	@InjectMocks
	private PtService ptService;

	@Mock
	private TraineeService traineeService;

	@Mock
	private TrainerService trainerService;

	@Mock
	private PtTrainerTraineeRepository ptTrainerTraineeRepository;

	@Mock
	private PtLessonSearchRepository ptLessonSearchRepository;

	@Test
	@DisplayName("트레이너와 연결 성공")
	void connectWithTrainer_success() {
		// given
		Long traineeMemberId = 20L;

		Long trainerId = 1L;
		Long traineeId = 2L;

		Member trainerMember = MemberFixture.getTrainerMember1();
		Member traineeMember = MemberFixture.getTraineeMember1();

		Trainer trainer = TrainerFixture.getTrainer1(trainerId, trainerMember);
		Trainee trainee = TraineeFixture.getTrainee1(traineeId, traineeMember);

		String invitationCode = "1A2V3C4D";
		LocalDate startDate = LocalDate.of(2025, 1, 1);
		int totalPtCount = 10;
		int finishedPtCount = 3;

		ConnectWithTrainerRequest request = new ConnectWithTrainerRequest(invitationCode, startDate, totalPtCount,
			finishedPtCount);

		given(trainerService.getTrainerWithInvitationCode(request.invitationCode())).willReturn(trainer);
		given(traineeService.getTraineeWithMemberId(traineeMemberId)).willReturn(trainee);
		given(ptTrainerTraineeRepository.existsByTraineeIdAndDeletedAtIsNull(traineeId)).willReturn(false);
		given(ptTrainerTraineeRepository.existsByTrainerIdAndTraineeIdAndDeletedAtIsNull(trainerId, traineeId))
			.willReturn(false);

		// when
		ConnectWithTrainerDto connectWithTrainerDto = ptService.connectWithTrainer(traineeMemberId, request);

		// then
		assertThat(connectWithTrainerDto.trainerFcmToken()).isEqualTo(trainerMember.getFcmToken());
	}

	@Test
	@DisplayName("트레이너와 연결 실패 - 이미 다른 트레이너와 연결 중")
	void connectWithTrainer_already_connected_with_other_trainer_fail() {
		// given
		Long traineeMemberId = 20L;

		Long trainerId = 1L;
		Long traineeId = 2L;

		Member trainerMember = MemberFixture.getTrainerMember1();
		Member traineeMember = MemberFixture.getTraineeMember1();

		Trainer trainer = TrainerFixture.getTrainer1(trainerId, trainerMember);
		Trainee trainee = TraineeFixture.getTrainee1(traineeId, traineeMember);

		String invitationCode = "1A2V3C4D";
		LocalDate startDate = LocalDate.of(2025, 1, 1);
		Integer totalPtCount = 10;
		Integer finishedPtCount = 3;

		ConnectWithTrainerRequest request = new ConnectWithTrainerRequest(invitationCode, startDate, totalPtCount,
			finishedPtCount);

		given(trainerService.getTrainerWithInvitationCode(request.invitationCode())).willReturn(trainer);
		given(traineeService.getTraineeWithMemberId(traineeMemberId)).willReturn(trainee);
		given(ptTrainerTraineeRepository.existsByTraineeIdAndDeletedAtIsNull(traineeId)).willReturn(true);

		// when & then
		assertThatThrownBy(() -> ptService.connectWithTrainer(traineeMemberId, request))
			.isInstanceOf(ConflictException.class);
	}

	@Test
	@DisplayName("트레이너와 연결 실패 - 이미 해당 트레이너와 연결 중")
	void connectWithTrainer_already_connected_with_trainer_fail() {
		// given
		Long traineeMemberId = 20L;

		Long trainerId = 1L;
		Long traineeId = 2L;

		Member trainerMember = MemberFixture.getTrainerMember1();
		Member traineeMember = MemberFixture.getTraineeMember1();

		Trainer trainer = TrainerFixture.getTrainer1(trainerId, trainerMember);
		Trainee trainee = TraineeFixture.getTrainee1(traineeId, traineeMember);

		String invitationCode = "1A2V3C4D";
		LocalDate startDate = LocalDate.of(2025, 1, 1);
		Integer totalPtCount = 10;
		Integer finishedPtCount = 3;

		ConnectWithTrainerRequest request = new ConnectWithTrainerRequest(invitationCode, startDate, totalPtCount,
			finishedPtCount);

		given(trainerService.getTrainerWithInvitationCode(request.invitationCode())).willReturn(trainer);
		given(traineeService.getTraineeWithMemberId(traineeMemberId)).willReturn(trainee);
		given(ptTrainerTraineeRepository.existsByTraineeIdAndDeletedAtIsNull(traineeId)).willReturn(false);
		given(ptTrainerTraineeRepository.existsByTrainerIdAndTraineeIdAndDeletedAtIsNull(trainerId, traineeId))
			.willReturn(true);

		// when & then
		assertThatThrownBy(() -> ptService.connectWithTrainer(traineeMemberId, request))
			.isInstanceOf(ConflictException.class);
	}

	@Test
	@DisplayName("트레이너 - 특정 날짜 수업 리스트 조회 성공")
	void getPtLessonsOnDate_success() {
		// given
		Long memberId = 1L;
		Long trainerId = 3L;
		Long traineeId = 2L;
		LocalDate date = LocalDate.of(2025, 1, 1);

		Member trainerMember = MemberFixture.getTrainerMember1();
		Member traineeMember = MemberFixture.getTraineeMember1();

		Trainer trainer = TrainerFixture.getTrainer1(trainerId, trainerMember);
		Trainee trainee = TraineeFixture.getTrainee1(traineeId, traineeMember);

		PtTrainerTrainee ptTrainerTrainee = PtTrainerTraineeFixture.getPtTrainerTrainee1(trainer, trainee);

		PtLesson ptLesson = PtLesson.builder()
			.id(1L)
			.ptTrainerTrainee(ptTrainerTrainee)
			.lessonStart(LocalDateTime.of(date, LocalTime.of(10, 0)))
			.lessonEnd(LocalDateTime.of(date, LocalTime.of(11, 0)))
			.build();

		ptLesson.completeLesson();

		given(trainerService.getTrainerWithMemberId(memberId)).willReturn(trainer);
		given(ptLessonSearchRepository.findAllByTrainerIdAndDate(trainerId, date)).willReturn(List.of(ptLesson));

		// when
		GetPtLessonsOnDateResponse result = ptService.getPtLessonsOnDate(memberId, date);

		// then
		assertThat(result.count()).isEqualTo(1);
		assertThat(result.lessons().getFirst().isCompleted()).isTrue();
		assertThat(result.lessons().getFirst().traineeId()).isEqualTo(String.valueOf(traineeId));
	}
}
