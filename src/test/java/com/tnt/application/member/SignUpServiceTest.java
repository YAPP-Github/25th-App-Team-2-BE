package com.tnt.application.member;

import static com.tnt.common.constant.ProfileConstant.TRAINER_DEFAULT_IMAGE;
import static com.tnt.common.error.model.ErrorMessage.MEMBER_CONFLICT;
import static com.tnt.domain.member.MemberType.TRAINER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tnt.application.trainee.PtGoalService;
import com.tnt.application.trainee.TraineeService;
import com.tnt.application.trainer.TrainerService;
import com.tnt.common.error.exception.ConflictException;
import com.tnt.domain.member.Member;
import com.tnt.domain.trainee.PtGoal;
import com.tnt.domain.trainee.Trainee;
import com.tnt.domain.trainer.Trainer;
import com.tnt.dto.member.request.SignUpRequest;
import com.tnt.dto.member.response.SignUpResponse;
import com.tnt.fixture.MemberFixture;
import com.tnt.gateway.service.SessionService;

@ExtendWith(MockitoExtension.class)
class SignUpServiceTest {

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

	@InjectMocks
	private SignUpService signUpService;

	@Test
	@DisplayName("트레이너 회원가입 성공")
	void save_trainer_success() {
		// given
		Member trainerMember = MemberFixture.getTrainerMember1WithId();

		given(memberService.saveMember(any(Member.class))).willReturn(trainerMember);
		given(trainerService.saveTrainer(any(Trainer.class))).willReturn(
			Trainer.builder().member(trainerMember).build());

		SignUpRequest request = new SignUpRequest(trainerMember.getFcmToken(), trainerMember.getMemberType(),
			trainerMember.getSocialType(), trainerMember.getSocialId(), trainerMember.getEmail(),
			trainerMember.getServiceAgreement(), trainerMember.getCollectionAgreement(),
			trainerMember.getAdvertisementAgreement(), trainerMember.getName(), trainerMember.getBirthday(), null, null,
			null, null);

		// when
		Long result = signUpService.signUp(request);

		// then
		assertThat(result).isNotNull().isEqualTo(trainerMember.getId());
		verify(memberService).saveMember(any(Member.class));
		verify(trainerService).saveTrainer(any(Trainer.class));
	}

	@Test
	@DisplayName("트레이니 회원가입 성공")
	void save_trainee_success() {
		// given
		Member traineeMember = MemberFixture.getTraineeMember1WithId();

		given(memberService.saveMember(any(Member.class))).willReturn(traineeMember);
		given(traineeService.saveTrainee(any(Trainee.class))).willReturn(
			Trainee.builder().id(1L).member(traineeMember).height(180.0).weight(75.0).build());
		given(ptGoalService.saveAllPtGoals(anyList())).willReturn(Stream.of("목표1", "목표2")
			.map(content -> PtGoal.builder().traineeId(traineeMember.getId()).content(content).build())
			.toList());

		SignUpRequest request = new SignUpRequest(traineeMember.getFcmToken(), traineeMember.getMemberType(),
			traineeMember.getSocialType(), traineeMember.getSocialId(), traineeMember.getEmail(),
			traineeMember.getServiceAgreement(), traineeMember.getCollectionAgreement(),
			traineeMember.getAdvertisementAgreement(), traineeMember.getName(), traineeMember.getBirthday(), 180.0,
			75.0, "주의사항", List.of("목표1", "목표2"));

		// when
		Long result = signUpService.signUp(request);

		// then
		assertThat(result).isNotNull().isEqualTo(traineeMember.getId());
		verify(memberService).saveMember(any(Member.class));
		verify(traineeService).saveTrainee(any(Trainee.class));
		verify(ptGoalService).saveAllPtGoals(any());
	}

	@Test
	@DisplayName("이미 존재하는 회원 가입 시도 실패")
	void exists_member_error() {
		// given
		Member trainerMember = MemberFixture.getTrainerMember1WithId();

		doThrow(new ConflictException(MEMBER_CONFLICT)).when(memberService).validateMemberNotExists(any(), any());

		SignUpRequest request = new SignUpRequest(trainerMember.getFcmToken(), trainerMember.getMemberType(),
			trainerMember.getSocialType(), trainerMember.getSocialId(), trainerMember.getEmail(),
			trainerMember.getServiceAgreement(), trainerMember.getCollectionAgreement(),
			trainerMember.getAdvertisementAgreement(), trainerMember.getName(), trainerMember.getBirthday(), null, null,
			null, null);

		// when & then
		assertThrows(ConflictException.class, () -> signUpService.signUp(request));
	}

	@Test
	@DisplayName("회원가입 완료 성공")
	void sign_up_success() {
		// given
		Member trainerMember = MemberFixture.getTrainerMember1WithId();

		given(memberService.getMemberWithMemberId(any())).willReturn(trainerMember);

		// when
		SignUpResponse response = signUpService.finishSignUpWithImage(TRAINER_DEFAULT_IMAGE, trainerMember.getId(),
			TRAINER);

		// then
		assertThat(response).isNotNull();
		assertThat(response.name()).isEqualTo(trainerMember.getName());
		assertThat(response.profileImageUrl()).isEqualTo(TRAINER_DEFAULT_IMAGE);
		assertThat(response.memberType()).isEqualTo(TRAINER);
		verify(sessionService).createSession(anyString(), anyString());
	}
}
