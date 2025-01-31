package com.tnt.application.member;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
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
import com.tnt.domain.member.Member;
import com.tnt.domain.pt.PtLesson;
import com.tnt.domain.pt.PtTrainerTrainee;
import com.tnt.domain.trainee.PtGoal;
import com.tnt.domain.trainee.Trainee;
import com.tnt.domain.trainer.Trainer;
import com.tnt.dto.member.request.WithdrawRequest;
import com.tnt.fixture.MemberFixture;
import com.tnt.fixture.PtTrainerTraineeFixture;
import com.tnt.fixture.TraineeFixture;
import com.tnt.fixture.TrainerFixture;
import com.tnt.gateway.service.OAuthService;
import com.tnt.gateway.service.SessionService;

@ExtendWith(MockitoExtension.class)
class WithdrawServiceTest {

	@Mock
	private SessionService sessionService;

	@Mock
	private OAuthService oAuthService;

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

	@InjectMocks
	private WithdrawService withdrawService;

	@Test
	@DisplayName("트레이너 회원 탈퇴 성공")
	void withdraw_trainer_success() {
		// given
		Member trainerMember = MemberFixture.getTrainerMember1WithId();
		Trainer trainer = TrainerFixture.getTrainer1(1L, trainerMember);

		WithdrawRequest request = new WithdrawRequest("accessToken", "authCode");

		given(memberService.getMemberWithMemberId(trainerMember.getId())).willReturn(trainerMember);
		given(trainerService.getTrainerWithMemberId(trainerMember.getId())).willReturn(trainer);
		given(ptService.getPtTrainerTraineeWithTrainerId(trainer.getId())).willReturn(null);

		// when
		withdrawService.withdraw(trainerMember.getId(), request);

		// then
		verify(trainerService).softDeleteTrainer(trainer);
		verify(memberService).softDeleteMember(trainerMember);
		verify(oAuthService).revoke(trainerMember.getSocialId(), trainerMember.getSocialType(), request);
		verify(sessionService).removeSession(String.valueOf(trainerMember.getId()));
	}

	@Test
	@DisplayName("트레이니 회원 탈퇴 성공")
	void withdraw_trainee_success() {
		// given
		Member traineeMember = MemberFixture.getTraineeMember1WithId();
		Trainee trainee = TraineeFixture.getTrainee1(1L, traineeMember);

		List<PtGoal> ptGoals = List.of(PtGoal.builder().id(1L).traineeId(trainee.getId()).content("test").build());

		WithdrawRequest request = new WithdrawRequest("accessToken", "authCode");

		given(memberService.getMemberWithMemberId(traineeMember.getId())).willReturn(traineeMember);
		given(traineeService.getTraineeWithMemberId(traineeMember.getId())).willReturn(trainee);
		given(ptGoalService.getAllPtGoalsWithTraineeId(trainee.getId())).willReturn(ptGoals);
		given(ptService.getPtTrainerTraineeWithTraineeId(trainee.getId())).willReturn(null);

		// when
		withdrawService.withdraw(traineeMember.getId(), request);

		// then
		verify(ptGoalService).softDeleteAllPtGoals(ptGoals);
		verify(traineeService).softDeleteTrainee(trainee);
		verify(memberService).softDeleteMember(traineeMember);
		verify(oAuthService).revoke(traineeMember.getSocialId(), traineeMember.getSocialType(), request);
		verify(sessionService).removeSession(String.valueOf(traineeMember.getId()));
	}

	@Test
	@DisplayName("지원하지 않는 회원 타입으로 탈퇴 시도시 실패")
	void withdraw_unsupported_member_type_error() {
		// given
		Long memberId = 1L;
		Member traineeMember = MemberFixture.getTraineeMember2WithId();

		WithdrawRequest request = new WithdrawRequest("accessToken", "authCode");

		// when
		given(memberService.getMemberWithMemberId(memberId)).willReturn(traineeMember);

		// then
		assertThrows(IllegalArgumentException.class,
			() -> withdrawService.withdraw(memberId, request));
	}

	@Test
	@DisplayName("PT 관계가 있는 트레이너 회원 탈퇴 성공")
	void withdraw_trainer_with_pt_success() {
		// given
		Member trainerMember = MemberFixture.getTrainerMember1WithId();
		Member traineeMember = MemberFixture.getTraineeMember2WithId();

		Trainer trainer = TrainerFixture.getTrainer1(1L, trainerMember);
		Trainee trainee = TraineeFixture.getTrainee1(1L, traineeMember);

		PtTrainerTrainee ptTrainerTrainee = PtTrainerTraineeFixture.getPtTrainerTrainee1(trainer, trainee);

		List<PtLesson> ptLessons = List.of(PtLesson.builder()
			.id(1L)
			.ptTrainerTrainee(ptTrainerTrainee)
			.lessonStart(LocalDateTime.now())
			.lessonEnd(LocalDateTime.now())
			.build());

		WithdrawRequest request = new WithdrawRequest("accessToken", "authCode");

		given(memberService.getMemberWithMemberId(trainerMember.getId())).willReturn(trainerMember);
		given(trainerService.getTrainerWithMemberId(trainerMember.getId())).willReturn(trainer);
		given(ptService.getPtTrainerTraineeWithTrainerId(trainer.getId())).willReturn(ptTrainerTrainee);
		given(ptService.getPtLessonWithPtTrainerTrainee(ptTrainerTrainee)).willReturn(ptLessons);

		// when
		withdrawService.withdraw(trainerMember.getId(), request);

		// then
		verify(ptService).softDeletePtTrainerTrainee(ptTrainerTrainee);
		verify(trainerService).softDeleteTrainer(trainer);
		verify(memberService).softDeleteMember(trainerMember);
		verify(oAuthService).revoke(trainerMember.getSocialId(), trainerMember.getSocialType(), request);
		verify(sessionService).removeSession(String.valueOf(trainerMember.getId()));
	}

	@Test
	@DisplayName("PT 관계가 있는 트레이니 회원 탈퇴 성공")
	void withdraw_trainee_with_pt_success() {
		// given
		Member trainerMember = MemberFixture.getTrainerMember1WithId();
		Member traineeMember = MemberFixture.getTraineeMember1WithId();

		Trainer trainer = TrainerFixture.getTrainer1(1L, trainerMember);
		Trainee trainee = TraineeFixture.getTrainee1(1L, traineeMember);

		PtTrainerTrainee ptTrainerTrainee = PtTrainerTraineeFixture.getPtTrainerTrainee1(trainer, trainee);

		List<PtGoal> ptGoals = List.of(PtGoal.builder().id(1L).traineeId(trainee.getId()).content("test").build());

		List<PtLesson> ptLessons = List.of(PtLesson.builder()
			.id(1L)
			.ptTrainerTrainee(ptTrainerTrainee)
			.lessonStart(LocalDateTime.now())
			.lessonEnd(LocalDateTime.now())
			.build());

		WithdrawRequest request = new WithdrawRequest("accessToken", "authCode");

		given(memberService.getMemberWithMemberId(traineeMember.getId())).willReturn(traineeMember);
		given(traineeService.getTraineeWithMemberId(traineeMember.getId())).willReturn(trainee);
		given(ptGoalService.getAllPtGoalsWithTraineeId(trainee.getId())).willReturn(ptGoals);
		given(ptService.getPtTrainerTraineeWithTraineeId(trainee.getId())).willReturn(ptTrainerTrainee);
		given(ptService.getPtLessonWithPtTrainerTrainee(ptTrainerTrainee)).willReturn(ptLessons);

		// when
		withdrawService.withdraw(traineeMember.getId(), request);

		// then
		verify(ptService).softDeletePtTrainerTrainee(ptTrainerTrainee);
		verify(ptGoalService).softDeleteAllPtGoals(ptGoals);
		verify(traineeService).softDeleteTrainee(trainee);
		verify(memberService).softDeleteMember(traineeMember);
		verify(oAuthService).revoke(traineeMember.getSocialId(), traineeMember.getSocialType(), request);
		verify(sessionService).removeSession(String.valueOf(traineeMember.getId()));
	}
}
