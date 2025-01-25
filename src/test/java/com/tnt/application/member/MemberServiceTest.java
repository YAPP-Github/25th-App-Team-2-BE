package com.tnt.application.member;

import static com.tnt.domain.constant.Constant.TRAINEE;
import static com.tnt.domain.constant.Constant.TRAINEE_DEFAULT_IMAGE;
import static com.tnt.domain.constant.Constant.TRAINER;
import static com.tnt.domain.constant.Constant.TRAINER_DEFAULT_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tnt.application.auth.SessionService;
import com.tnt.domain.member.Member;
import com.tnt.domain.member.SocialType;
import com.tnt.domain.trainee.PtGoal;
import com.tnt.domain.trainee.Trainee;
import com.tnt.domain.trainer.Trainer;
import com.tnt.dto.member.request.SignUpRequest;
import com.tnt.dto.member.response.SignUpResponse;
import com.tnt.global.error.exception.ConflictException;
import com.tnt.infrastructure.mysql.repository.member.MemberRepository;
import com.tnt.infrastructure.mysql.repository.trainee.PtGoalRepository;
import com.tnt.infrastructure.mysql.repository.trainee.TraineeRepository;
import com.tnt.infrastructure.mysql.repository.trainer.TrainerRepository;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

	private static final String MOCK_FCM_TOKEN = "fcm-token";
	private static final String MOCK_SOCIAL_ID = "12345";
	private static final String MOCK_EMAIL = "test@kakao.com";
	private static final String MOCK_NAME = "홍길동";
	private static final String MOCK_CAUTION = "주의사항";
	private static final List<String> MOCK_GOALS = List.of("목표1", "목표2");

	@InjectMocks
	private MemberService memberService;
	@Mock
	private MemberRepository memberRepository;
	@Mock
	private TrainerRepository trainerRepository;
	@Mock
	private TraineeRepository traineeRepository;
	@Mock
	private PtGoalRepository ptGoalRepository;
	@Mock
	private SessionService sessionService;

	private SignUpRequest trainerRequest;
	private SignUpRequest traineeRequest;
	private Member mockTrainerMember;
	private Member mockTraineeMember;
	private Trainee mockTrainee;
	private List<PtGoal> mockPtGoals;

	private Member createMockMember(Long id, String profileImageUrl) {
		return Member.builder()
			.id(id)
			.socialId(MOCK_SOCIAL_ID)
			.email(MOCK_EMAIL)
			.name(MOCK_NAME)
			.profileImageUrl(profileImageUrl)
			.serviceAgreement(true)
			.collectionAgreement(true)
			.advertisementAgreement(true)
			.socialType(SocialType.KAKAO)
			.build();
	}

	@BeforeEach
	void setUp() {
		trainerRequest = new SignUpRequest(MOCK_FCM_TOKEN, TRAINER, "KAKAO", MOCK_SOCIAL_ID, MOCK_EMAIL, true, true,
			true, MOCK_NAME, null, null, null, null, null);
		traineeRequest = new SignUpRequest(MOCK_FCM_TOKEN, TRAINEE, "KAKAO", MOCK_SOCIAL_ID, MOCK_EMAIL, true, true,
			true, MOCK_NAME, null, 180.0, 75.0, MOCK_CAUTION, MOCK_GOALS);

		mockTrainerMember = createMockMember(1L, TRAINER_DEFAULT_IMAGE);
		mockTraineeMember = createMockMember(2L, TRAINEE_DEFAULT_IMAGE);

		mockTrainee = Trainee.builder()
			.id(1L)
			.member(mockTraineeMember)
			.height(traineeRequest.height())
			.weight(traineeRequest.weight())
			.cautionNote(traineeRequest.cautionNote())
			.build();

		mockPtGoals = MOCK_GOALS.stream()
			.map(content -> PtGoal.builder()
				.traineeId(mockTrainee.getId())
				.content(content)
				.build())
			.toList();
	}

	@Nested
	@DisplayName("saveMember 테스트")
	class SaveMemberTest {

		@Test
		@DisplayName("트레이너 회원가입 성공")
		void save_trainer_success() {
			// given
			given(memberRepository.findBySocialIdAndSocialTypeAndDeletedAtIsNull(any(), any())).willReturn(
				Optional.empty());
			given(memberRepository.save(any(Member.class))).willReturn(mockTrainerMember);
			given(trainerRepository.save(any(Trainer.class))).willReturn(
				Trainer.builder().member(mockTrainerMember).build());

			// when
			Long result = memberService.signUp(trainerRequest);

			// then
			assertThat(result).isNotNull().isEqualTo(mockTrainerMember.getId());
			verify(memberRepository).save(any(Member.class));
			verify(trainerRepository).save(any(Trainer.class));
		}

		@Test
		@DisplayName("트레이니 회원가입 성공")
		void save_trainee_success() {
			// given
			given(memberRepository.findBySocialIdAndSocialTypeAndDeletedAtIsNull(any(), any())).willReturn(
				Optional.empty());
			given(memberRepository.save(any(Member.class))).willReturn(mockTraineeMember);
			given(traineeRepository.save(any(Trainee.class))).willReturn(mockTrainee);
			given(ptGoalRepository.saveAll(any())).willReturn(mockPtGoals);

			// when
			Long result = memberService.signUp(traineeRequest);

			// then
			assertThat(result).isNotNull().isEqualTo(mockTraineeMember.getId());
			verify(memberRepository).save(any(Member.class));
			verify(traineeRepository).save(any(Trainee.class));
			verify(ptGoalRepository).saveAll(any());
		}

		@Test
		@DisplayName("이미 존재하는 회원 가입 시도 실패")
		void save_member_already_exists_fail() {
			// given
			given(memberRepository.findBySocialIdAndSocialTypeAndDeletedAtIsNull(any(), any())).willReturn(
				Optional.of(mockTrainerMember));

			// when & then
			assertThrows(ConflictException.class, () -> memberService.signUp(trainerRequest));
		}

		@Test
		@DisplayName("지원하지 않는 회원 타입으로 가입 시도 실패")
		void save_member_unsupported_type_fail() {
			// given
			SignUpRequest invalidRequest = new SignUpRequest(MOCK_FCM_TOKEN, "invalid_type", "KAKAO", MOCK_SOCIAL_ID,
				MOCK_EMAIL, true, true, true, MOCK_NAME, null, null, null, null, null);

			// when & then
			assertThrows(IllegalArgumentException.class, () -> memberService.signUp(invalidRequest));
		}
	}

	@Nested
	@DisplayName("signUp 테스트")
	class SignUpTest {

		@Test
		@DisplayName("회원가입 완료 성공")
		void sign_up_success() {
			// given
			given(memberRepository.findByIdAndDeletedAtIsNull(any())).willReturn(Optional.of(mockTrainerMember));

			// when
			SignUpResponse response = memberService.finishSignUpWithImage(TRAINER_DEFAULT_IMAGE,
				mockTrainerMember.getId(), TRAINER);

			// then
			assertThat(response).isNotNull();
			assertThat(response.name()).isEqualTo(MOCK_NAME);
			assertThat(response.profileImageUrl()).isEqualTo(TRAINER_DEFAULT_IMAGE);
			assertThat(response.memberType()).isEqualTo(TRAINER);
			verify(sessionService).createSession(anyString(), anyString());
		}
	}

	@Nested
	@DisplayName("회원가입 프로세스 통합 테스트")
	class SignUpProcessTest {

		@Test
		@DisplayName("트레이너 회원가입 전체 프로세스 성공")
		void trainer_sign_up_process_success() {
			// given
			given(memberRepository.findBySocialIdAndSocialTypeAndDeletedAtIsNull(any(), any())).willReturn(
				Optional.empty());
			given(memberRepository.save(any(Member.class))).willReturn(mockTrainerMember);
			given(trainerRepository.save(any(Trainer.class))).willReturn(
				Trainer.builder().member(mockTrainerMember).build());
			given(memberRepository.findByIdAndDeletedAtIsNull(any())).willReturn(Optional.of(mockTrainerMember));

			// when
			Long savedMemberId = memberService.signUp(trainerRequest);
			SignUpResponse response = memberService.finishSignUpWithImage(TRAINER_DEFAULT_IMAGE, savedMemberId,
				TRAINER);

			// then
			assertThat(response).isNotNull();
			assertThat(response.memberType()).isEqualTo(TRAINER);
			assertThat(response.name()).isEqualTo(MOCK_NAME);
			assertThat(response.profileImageUrl()).isEqualTo(TRAINER_DEFAULT_IMAGE);
			verify(sessionService).createSession(anyString(), anyString());
		}

		@Test
		@DisplayName("트레이니 회원가입 전체 프로세스 성공")
		void trainee_sign_up_process_success() {
			// given
			given(memberRepository.findBySocialIdAndSocialTypeAndDeletedAtIsNull(any(), any())).willReturn(
				Optional.empty());
			given(memberRepository.save(any(Member.class))).willReturn(mockTraineeMember);
			given(traineeRepository.save(any(Trainee.class))).willReturn(mockTrainee);
			given(ptGoalRepository.saveAll(any())).willReturn(mockPtGoals);
			given(memberRepository.findByIdAndDeletedAtIsNull(any())).willReturn(Optional.of(mockTrainerMember));

			// when
			Long savedMemberId = memberService.signUp(traineeRequest);
			SignUpResponse response = memberService.finishSignUpWithImage(TRAINEE_DEFAULT_IMAGE, savedMemberId,
				TRAINEE);

			// then
			assertThat(response).isNotNull();
			assertThat(response.memberType()).isEqualTo(TRAINEE);
			assertThat(response.name()).isEqualTo(MOCK_NAME);
			assertThat(response.profileImageUrl()).isEqualTo(TRAINEE_DEFAULT_IMAGE);
			verify(sessionService).createSession(anyString(), anyString());
		}

		@Test
		@DisplayName("회원가입 중 세션 생성 실패")
		void sign_up_process_session_fail() {
			// given
			given(memberRepository.findBySocialIdAndSocialTypeAndDeletedAtIsNull(any(), any())).willReturn(
				Optional.empty());
			given(memberRepository.save(any(Member.class))).willReturn(mockTrainerMember);
			given(trainerRepository.save(any(Trainer.class))).willReturn(
				Trainer.builder().member(mockTrainerMember).build());
			given(memberRepository.findByIdAndDeletedAtIsNull(any())).willReturn(Optional.of(mockTrainerMember));
			doThrow(new RuntimeException("세션 생성 실패")).when(sessionService)
				.createSession(any(), eq(String.valueOf(mockTrainerMember.getId())));

			// when
			Long savedMemberId = memberService.signUp(trainerRequest);

			// then
			assertThat(savedMemberId).isNotNull();
			assertThrows(RuntimeException.class,
				() -> memberService.finishSignUpWithImage(TRAINER_DEFAULT_IMAGE, savedMemberId, TRAINER));
			verify(sessionService).createSession(any(), eq(String.valueOf(mockTrainerMember.getId())));
		}
	}
}
