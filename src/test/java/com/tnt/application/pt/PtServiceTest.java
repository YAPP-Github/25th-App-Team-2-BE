package com.tnt.application.pt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.util.Optional;

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
import com.tnt.domain.pt.PtTrainerTrainee;
import com.tnt.domain.trainee.Trainee;
import com.tnt.domain.trainer.Trainer;
import com.tnt.dto.trainer.ConnectWithTrainerDto;
import com.tnt.dto.trainer.request.ConnectWithTrainerRequest;
import com.tnt.fixture.MemberFixture;
import com.tnt.fixture.PtTrainerTraineeFixture;
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

	@Test
	@DisplayName("트레이너와 연결 성공")
	void connectWithTrainer_success() {
		// given
		Long traineeMemberId = 20L;

		Long trainerId = 1L;
		Long traineeId = 2L;

		Member trainerMember = MemberFixture.getTrainerMember1();
		Member traineeMember = MemberFixture.getTraineeMember1();

		Trainer trainer = Trainer.builder()
			.id(trainerId)
			.member(trainerMember)
			.build();

		Trainee trainee = Trainee.builder()
			.id(traineeId)
			.member(traineeMember)
			.height(180.4)
			.weight(70.5)
			.cautionNote("주의사항")
			.build();

		String invitationCode = "1A2V3C4D";
		LocalDate startDate = LocalDate.of(2025, 1, 1);
		int totalPtCount = 10;
		int finishedPtCount = 3;

		ConnectWithTrainerRequest request = new ConnectWithTrainerRequest(invitationCode, startDate, totalPtCount,
			finishedPtCount);

		given(trainerService.getTrainerWithInvitationCode(request.invitationCode())).willReturn(trainer);
		given(traineeService.getTraineeWithMemberId(String.valueOf(traineeMemberId))).willReturn(trainee);
		given(ptTrainerTraineeRepository.findByTraineeIdAndDeletedAtIsNull(traineeId)).willReturn(
			Optional.empty());
		given(ptTrainerTraineeRepository.findByTrainerIdAndTraineeIdAndDeletedAtIsNull(trainerId,
			traineeId)).willReturn(Optional.empty());

		// when
		ConnectWithTrainerDto connectWithTrainerDto = ptService.connectWithTrainer(String.valueOf(traineeMemberId),
			request);

		// then
		assertThat(connectWithTrainerDto.trainerFcmToken()).isEqualTo(trainerMember.getFcmToken());
	}

	@Test
	@DisplayName("트레이너와 연결 실패 - 이미 다른 트레이너와 연결 중")
	void connectWithTrainer_already_connected_with_other_trainer_fail() {
		// given
		Long traineeMemberId = 20L;

		Long trainerId = 1L;
		Long otherTrainerId = 99L;
		Long traineeId = 2L;

		Member trainerMember = MemberFixture.getTrainerMember1();
		Member traineeMember = MemberFixture.getTraineeMember1();

		Trainer trainer = Trainer.builder()
			.id(trainerId)
			.member(trainerMember)
			.build();

		Trainee trainee = Trainee.builder()
			.id(traineeId)
			.member(traineeMember)
			.height(180.4)
			.weight(70.5)
			.cautionNote("주의사항")
			.build();

		String invitationCode = "1A2V3C4D";
		LocalDate startDate = LocalDate.of(2025, 1, 1);
		Integer totalPtCount = 10;
		Integer finishedPtCount = 3;

		ConnectWithTrainerRequest request = new ConnectWithTrainerRequest(invitationCode, startDate, totalPtCount,
			finishedPtCount);

		PtTrainerTrainee ptTrainerTrainee = PtTrainerTraineeFixture.getPtTrainerTrainee1(otherTrainerId, traineeId);

		given(trainerService.getTrainerWithInvitationCode(request.invitationCode())).willReturn(trainer);
		given(traineeService.getTraineeWithMemberId(String.valueOf(traineeMemberId))).willReturn(trainee);
		given(ptTrainerTraineeRepository.findByTraineeIdAndDeletedAtIsNull(traineeId)).willReturn(
			Optional.of(ptTrainerTrainee));

		String traineeMemberIdStr = String.valueOf(traineeMemberId);

		// when & then
		assertThatThrownBy(() -> ptService.connectWithTrainer(traineeMemberIdStr, request))
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

		Trainer trainer = Trainer.builder()
			.id(trainerId)
			.member(trainerMember)
			.build();

		Trainee trainee = Trainee.builder()
			.id(traineeId)
			.member(traineeMember)
			.height(180.4)
			.weight(70.5)
			.cautionNote("주의사항")
			.build();

		String invitationCode = "1A2V3C4D";
		LocalDate startDate = LocalDate.of(2025, 1, 1);
		Integer totalPtCount = 10;
		Integer finishedPtCount = 3;

		ConnectWithTrainerRequest request = new ConnectWithTrainerRequest(invitationCode, startDate, totalPtCount,
			finishedPtCount);

		PtTrainerTrainee ptTrainerTrainee = PtTrainerTraineeFixture.getPtTrainerTrainee1(trainerId, traineeId);

		given(trainerService.getTrainerWithInvitationCode(request.invitationCode())).willReturn(trainer);
		given(traineeService.getTraineeWithMemberId(String.valueOf(traineeMemberId))).willReturn(trainee);
		given(ptTrainerTraineeRepository.findByTraineeIdAndDeletedAtIsNull(traineeId)).willReturn(Optional.empty());
		given(ptTrainerTraineeRepository.findByTrainerIdAndTraineeIdAndDeletedAtIsNull(trainerId,
			traineeId)).willReturn(Optional.of(ptTrainerTrainee));

		String traineeMemberIdStr = String.valueOf(traineeMemberId);

		// when & then
		assertThatThrownBy(() -> ptService.connectWithTrainer(traineeMemberIdStr, request))
			.isInstanceOf(ConflictException.class);
	}

	@Test
	@DisplayName("PT 연결 정보 soft delete 성공")
	void soft_delete_pt_trainer_trainee_success() {
		// given
		Long trainerId = 1L;
		Long traineeId = 2L;

		PtTrainerTrainee ptTrainerTrainee = PtTrainerTraineeFixture.getPtTrainerTrainee1(trainerId, traineeId);

		// when
		ptService.softDeletePtTrainerTrainee(ptTrainerTrainee);

		// then
		assertThat(ptTrainerTrainee.getDeletedAt()).isNotNull();
	}
}
