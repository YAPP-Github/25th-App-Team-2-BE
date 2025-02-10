package com.tnt.application.member;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tnt.application.pt.PtService;
import com.tnt.application.trainee.PtGoalService;
import com.tnt.application.trainee.TraineeService;
import com.tnt.application.trainer.TrainerService;
import com.tnt.common.error.exception.NotFoundException;
import com.tnt.domain.member.Member;
import com.tnt.domain.pt.PtLesson;
import com.tnt.domain.pt.PtTrainerTrainee;
import com.tnt.domain.trainee.PtGoal;
import com.tnt.domain.trainee.Trainee;
import com.tnt.domain.trainer.Trainer;
import com.tnt.fixture.MemberFixture;
import com.tnt.fixture.PtLessonsFixture;
import com.tnt.fixture.PtTrainerTraineeFixture;
import com.tnt.fixture.TraineeFixture;
import com.tnt.fixture.TrainerFixture;
import com.tnt.gateway.service.SessionService;
import com.tnt.infrastructure.mysql.repository.pt.PtTrainerTraineeRepository;

@ExtendWith(MockitoExtension.class)
class WithdrawServiceTest {

	@Mock
	private SessionService sessionService;

	@Mock
	private MemberService memberService;

	@Mock
	private TrainerService trainerService;

	@Mock
	private TraineeService traineeService;

	@Mock
	private PtGoalService ptGoalService;

	@Mock
	private PtService ptService;

	@Mock
	private PtTrainerTraineeRepository ptTrainerTraineeRepository;

	@InjectMocks
	private WithdrawService withdrawService;

	@Test
	@DisplayName("트레이너 회원 탈퇴 성공")
	void withdraw_trainer_success() {
		// given
		Member trainerMember = MemberFixture.getTrainerMember1WithId();
		Trainer trainer = TrainerFixture.getTrainer1(1L, trainerMember);

		given(memberService.getMemberWithMemberId(trainerMember.getId())).willReturn(trainerMember);
		given(trainerService.getTrainerWithMemberId(trainerMember.getId())).willReturn(trainer);

		// when
		withdrawService.withdraw(trainerMember.getId());

		// then
		verify(sessionService).removeSession(String.valueOf(trainerMember.getId()));
	}

	@Test
	@DisplayName("트레이니 회원 탈퇴 성공")
	void withdraw_trainee_success() {
		// given
		Member traineeMember = MemberFixture.getTraineeMember1WithId();
		Trainee trainee = TraineeFixture.getTrainee1WithId(1L, traineeMember);

		List<PtGoal> ptGoals = List.of(PtGoal.builder().id(1L).traineeId(trainee.getId()).content("test").build());

		given(memberService.getMemberWithMemberId(traineeMember.getId())).willReturn(traineeMember);
		given(traineeService.getTraineeWithMemberId(traineeMember.getId())).willReturn(trainee);
		given(ptGoalService.getAllPtGoalsWithTraineeId(trainee.getId())).willReturn(ptGoals);

		// when
		withdrawService.withdraw(traineeMember.getId());

		// then
		verify(sessionService).removeSession(String.valueOf(traineeMember.getId()));
	}

	@Test
	@DisplayName("PT 관계가 있는 트레이너 회원 탈퇴 성공")
	void withdraw_trainer_with_pt_success() {
		// given
		Member trainerMember = MemberFixture.getTrainerMember1WithId();
		Member traineeMember = MemberFixture.getTraineeMember2WithId();

		Trainer trainer = TrainerFixture.getTrainer1(1L, trainerMember);
		Trainee trainee = TraineeFixture.getTrainee1WithId(1L, traineeMember);

		PtTrainerTrainee ptTrainerTrainee = PtTrainerTraineeFixture.getPtTrainerTrainee1(trainer, trainee);

		List<PtLesson> ptLessons = PtLessonsFixture.getPtLessons(ptTrainerTrainee);

		given(memberService.getMemberWithMemberId(trainerMember.getId())).willReturn(trainerMember);
		given(trainerService.getTrainerWithMemberId(trainerMember.getId())).willReturn(trainer);
		given(ptService.isPtTrainerTraineeExistWithTrainerId(trainer.getId())).willReturn(true);
		given(ptService.getPtTrainerTraineeWithTrainerId(trainer.getId())).willReturn(ptTrainerTrainee);
		given(ptService.getPtLessonWithPtTrainerTrainee(ptTrainerTrainee)).willReturn(ptLessons);

		// when
		withdrawService.withdraw(trainerMember.getId());

		// then
		verify(sessionService).removeSession(String.valueOf(trainerMember.getId()));
	}

	@Test
	@DisplayName("PT 관계가 있는 트레이니 회원 탈퇴 성공")
	void withdraw_trainee_with_pt_success() {
		// given
		Member trainerMember = MemberFixture.getTrainerMember1WithId();
		Member traineeMember = MemberFixture.getTraineeMember1WithId();

		Trainer trainer = TrainerFixture.getTrainer1(1L, trainerMember);
		Trainee trainee = TraineeFixture.getTrainee1WithId(1L, traineeMember);

		PtTrainerTrainee ptTrainerTrainee = PtTrainerTraineeFixture.getPtTrainerTrainee1(trainer, trainee);

		List<PtGoal> ptGoals = List.of(PtGoal.builder().id(1L).traineeId(trainee.getId()).content("test").build());

		List<PtLesson> ptLessons = PtLessonsFixture.getPtLessons(ptTrainerTrainee);

		given(memberService.getMemberWithMemberId(traineeMember.getId())).willReturn(traineeMember);
		given(traineeService.getTraineeWithMemberId(traineeMember.getId())).willReturn(trainee);
		given(ptService.isPtTrainerTraineeExistWithTraineeId(trainee.getId())).willReturn(true);
		given(ptGoalService.getAllPtGoalsWithTraineeId(trainee.getId())).willReturn(ptGoals);
		given(ptService.getPtTrainerTraineeWithTraineeId(trainee.getId())).willReturn(ptTrainerTrainee);
		given(ptService.getPtLessonWithPtTrainerTrainee(ptTrainerTrainee)).willReturn(ptLessons);

		// when
		withdrawService.withdraw(traineeMember.getId());

		// then
		verify(sessionService).removeSession(String.valueOf(traineeMember.getId()));
	}

	@Test
	@DisplayName("PT 관계가 없는 트레이너 회원 탈퇴 성공")
	void withdraw_trainer_without_pt_success() {
		// given
		Member trainerMember = MemberFixture.getTrainerMember1WithId();

		Trainer trainer = TrainerFixture.getTrainer1(1L, trainerMember);

		given(memberService.getMemberWithMemberId(trainerMember.getId())).willReturn(trainerMember);
		given(trainerService.getTrainerWithMemberId(trainerMember.getId())).willReturn(trainer);
		given(ptService.isPtTrainerTraineeExistWithTrainerId(trainer.getId())).willReturn(true);
		given(ptService.getPtTrainerTraineeWithTrainerId(trainer.getId())).willThrow(NotFoundException.class);

		// when
		withdrawService.withdraw(trainerMember.getId());

		// then
		verify(sessionService).removeSession(String.valueOf(trainerMember.getId()));
	}

	@Test
	@DisplayName("PT 관계가 없는 트레이니 회원 탈퇴 성공")
	void withdraw_trainee_without_pt_success() {
		// given
		Member traineeMember = MemberFixture.getTraineeMember1WithId();

		Trainee trainee = TraineeFixture.getTrainee1WithId(1L, traineeMember);

		List<PtGoal> ptGoals = List.of(PtGoal.builder().id(1L).traineeId(trainee.getId()).content("test").build());

		given(memberService.getMemberWithMemberId(traineeMember.getId())).willReturn(traineeMember);
		given(traineeService.getTraineeWithMemberId(traineeMember.getId())).willReturn(trainee);
		given(ptService.isPtTrainerTraineeExistWithTraineeId(trainee.getId())).willReturn(true);
		given(ptGoalService.getAllPtGoalsWithTraineeId(trainee.getId())).willReturn(ptGoals);
		given(ptService.getPtTrainerTraineeWithTraineeId(trainee.getId())).willThrow(NotFoundException.class);

		// when
		withdrawService.withdraw(traineeMember.getId());

		// then
		verify(sessionService).removeSession(String.valueOf(traineeMember.getId()));
	}
}
