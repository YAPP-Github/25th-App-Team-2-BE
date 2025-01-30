package com.tnt.application.member;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
import com.tnt.domain.pt.PtTrainerTrainee;
import com.tnt.domain.trainee.PtGoal;
import com.tnt.domain.trainee.Trainee;
import com.tnt.domain.trainer.Trainer;
import com.tnt.dto.member.request.WithdrawRequest;
import com.tnt.fixture.MemberFixture;
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
		Trainer trainer = Trainer.builder()
			.id(1L)
			.member(trainerMember)
			.build();

		WithdrawRequest request = new WithdrawRequest("accessToken", "authCode");

		given(memberService.getMemberWithMemberId(String.valueOf(trainerMember.getId()))).willReturn(trainerMember);
		given(trainerService.getTrainerWithMemberId(trainerMember.getId().toString())).willReturn(trainer);
		given(ptService.getPtTrainerTraineeWithTrainerId(trainer.getId())).willReturn(Optional.empty());

		// when
		withdrawService.withdraw(String.valueOf(trainerMember.getId()), request);

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
		Trainee trainee = Trainee.builder()
			.id(1L)
			.member(traineeMember)
			.height(180.0)
			.weight(75.0)
			.build();

		List<PtGoal> ptGoals = List.of(PtGoal.builder().id(1L).traineeId(trainee.getId()).content("test").build());

		WithdrawRequest request = new WithdrawRequest("accessToken", "authCode");

		given(memberService.getMemberWithMemberId(String.valueOf(traineeMember.getId()))).willReturn(traineeMember);
		given(traineeService.getTraineeWithMemberId(traineeMember.getId().toString())).willReturn(trainee);
		given(ptGoalService.getAllPtGoalsWithTraineeId(trainee.getId())).willReturn(ptGoals);
		given(ptService.getPtTrainerTraineeWithTraineeId(trainee.getId())).willReturn(Optional.empty());

		// when
		withdrawService.withdraw(String.valueOf(traineeMember.getId()), request);

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
		String memberId = String.valueOf(1L);
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
		Trainer trainer = Trainer.builder()
			.id(1L)
			.member(trainerMember)
			.build();

		PtTrainerTrainee ptTrainerTrainee = PtTrainerTrainee.builder()
			.trainerId(trainer.getId())
			.traineeId(2L)
			.startedAt(LocalDate.now())
			.finishedPtCount(0)
			.totalPtCount(10)
			.build();

		WithdrawRequest request = new WithdrawRequest("accessToken", "authCode");

		given(memberService.getMemberWithMemberId(String.valueOf(trainerMember.getId()))).willReturn(trainerMember);
		given(trainerService.getTrainerWithMemberId(trainerMember.getId().toString())).willReturn(trainer);
		given(ptService.getPtTrainerTraineeWithTrainerId(trainer.getId())).willReturn(Optional.of(ptTrainerTrainee));

		// when
		withdrawService.withdraw(String.valueOf(trainerMember.getId()), request);

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
		Member traineeMember = MemberFixture.getTraineeMember1WithId();
		Trainee trainee = Trainee.builder()
			.id(1L)
			.member(traineeMember)
			.height(180.0)
			.weight(75.0)
			.build();

		List<PtGoal> ptGoals = List.of(PtGoal.builder().id(1L).traineeId(trainee.getId()).content("test").build());

		PtTrainerTrainee ptTrainerTrainee = PtTrainerTrainee.builder()
			.trainerId(2L)
			.traineeId(trainee.getId())
			.startedAt(LocalDate.now())
			.finishedPtCount(0)
			.totalPtCount(10)
			.build();

		WithdrawRequest request = new WithdrawRequest("accessToken", "authCode");

		given(memberService.getMemberWithMemberId(String.valueOf(traineeMember.getId()))).willReturn(traineeMember);
		given(traineeService.getTraineeWithMemberId(traineeMember.getId().toString())).willReturn(trainee);
		given(ptGoalService.getAllPtGoalsWithTraineeId(trainee.getId())).willReturn(ptGoals);
		given(ptService.getPtTrainerTraineeWithTraineeId(trainee.getId())).willReturn(Optional.of(ptTrainerTrainee));

		// when
		withdrawService.withdraw(String.valueOf(traineeMember.getId()), request);

		// then
		verify(ptService).softDeletePtTrainerTrainee(ptTrainerTrainee);
		verify(ptGoalService).softDeleteAllPtGoals(ptGoals);
		verify(traineeService).softDeleteTrainee(trainee);
		verify(memberService).softDeleteMember(traineeMember);
		verify(oAuthService).revoke(traineeMember.getSocialId(), traineeMember.getSocialType(), request);
		verify(sessionService).removeSession(String.valueOf(traineeMember.getId()));
	}
}
