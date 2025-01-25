package com.tnt.presentation.trainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tnt.domain.member.Member;
import com.tnt.domain.member.SocialType;
import com.tnt.domain.pt.PtTrainerTrainee;
import com.tnt.domain.trainee.PtGoal;
import com.tnt.domain.trainee.Trainee;
import com.tnt.domain.trainer.Trainer;
import com.tnt.fixture.MemberFixture;
import com.tnt.infrastructure.mysql.repository.member.MemberRepository;
import com.tnt.infrastructure.mysql.repository.pt.PtTrainerTraineeRepository;
import com.tnt.infrastructure.mysql.repository.trainee.PtGoalRepository;
import com.tnt.infrastructure.mysql.repository.trainee.TraineeRepository;
import com.tnt.infrastructure.mysql.repository.trainer.TrainerRepository;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class TrainerControllerTest {

	private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private TrainerRepository trainerRepository;

	@Autowired
	private TraineeRepository traineeRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PtTrainerTraineeRepository ptTrainerTraineeRepository;

	@Autowired
	private PtGoalRepository ptGoalRepository;

	@Test
	@DisplayName("통합 테스트 - 트레이너 초대 코드 불러오기 성공")
	@WithMockUser(username = "1")
	void get_invitation_code_success() throws Exception {
		// given
		Long memberId = 1L;
		String socialId = "1234567890";
		String email = "abc@gmail.com";
		String name = "김영명";
		String profileImageUrl = "https://profile.com/1234567890";

		Member member = Member.builder()
			.id(memberId)
			.socialId(socialId)
			.email(email)
			.name(name)
			.profileImageUrl(profileImageUrl)
			.serviceAgreement(true)
			.collectionAgreement(true)
			.advertisementAgreement(true)
			.pushAgreement(true)
			.socialType(SocialType.KAKAO)
			.build();

		Trainer trainer = Trainer.builder()
			.member(member)
			.build();

		trainerRepository.save(trainer);

		// when & then
		mockMvc.perform(get("/trainers/invitation-code")).andExpect(status().isOk());
	}

	@Test
	@DisplayName("통합 테스트 - 트레이너 초대 코드 불러오기 실패 - 존재하지 않는 계정")
	@WithMockUser(username = "2")
	void get_invitation_code_fail() throws Exception {
		// given
		Long memberId = 1L;
		String socialId = "1234567890";
		String email = "abc@gmail.com";
		String name = "김영명";
		String profileImageUrl = "https://profile.com/1234567890";

		Member member = Member.builder()
			.id(memberId)
			.socialId(socialId)
			.email(email)
			.name(name)
			.profileImageUrl(profileImageUrl)
			.serviceAgreement(true)
			.collectionAgreement(true)
			.advertisementAgreement(true)
			.pushAgreement(true)
			.socialType(SocialType.KAKAO)
			.build();

		Trainer trainer = Trainer.builder()
			.member(member)
			.build();

		trainerRepository.save(trainer);

		// when & then
		mockMvc.perform(get("/trainers/invitation-code")).andExpect(status().is4xxClientError());
	}

	@Test
	@DisplayName("통합 테스트 - 트레이너 초대 코드 재발급 성공")
	@WithMockUser(username = "3")
	void reissue_invitation_code_success() throws Exception {
		// given
		Long memberId = 3L;
		String socialId = "1234567890";
		String email = "abc@gmail.com";
		String name = "김영명";
		String profileImageUrl = "https://profile.com/1234567890";

		Member member = Member.builder()
			.id(memberId)
			.socialId(socialId)
			.email(email)
			.name(name)
			.profileImageUrl(profileImageUrl)
			.serviceAgreement(true)
			.collectionAgreement(true)
			.advertisementAgreement(true)
			.pushAgreement(true)
			.socialType(SocialType.KAKAO)
			.build();

		Trainer trainer = Trainer.builder()
			.member(member)
			.build();

		trainerRepository.save(trainer);

		// when & then
		mockMvc.perform(put("/trainers/invitation-code/reissue")).andExpect(status().isCreated());
	}

	@Test
	@DisplayName("통합 테스트 - 트레이너 초대 코드 인증 성공")
	void verify_invitation_code_success() throws Exception {
		// given
		Long memberId = 4L;
		String socialId = "1234567890";
		String email = "abc@gmail.com";
		String name = "김영명";
		String profileImageUrl = "https://profile.com/1234567890";

		Member member = Member.builder()
			.id(memberId)
			.socialId(socialId)
			.email(email)
			.name(name)
			.profileImageUrl(profileImageUrl)
			.serviceAgreement(true)
			.collectionAgreement(true)
			.advertisementAgreement(true)
			.pushAgreement(true)
			.socialType(SocialType.KAKAO)
			.build();

		Trainer trainer = Trainer.builder()
			.member(member)
			.build();

		trainerRepository.save(trainer);

		String invitationCode = trainer.getInvitationCode();

		// when & then
		mockMvc.perform(get("/trainers/invitation-code/verify/" + invitationCode))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isVerified").value(true));
	}

	@Test
	@DisplayName("통합 테스트 - 트레이너 초대 코드 인증 실패")
	void verify_invitation_code_fail() throws Exception {
		// given
		Long memberId = 5L;
		String socialId = "1234567890";
		String email = "abc@gmail.com";
		String name = "김영명";
		String profileImageUrl = "https://profile.com/1234567890";

		Member member = Member.builder()
			.id(memberId)
			.socialId(socialId)
			.email(email)
			.name(name)
			.profileImageUrl(profileImageUrl)
			.serviceAgreement(true)
			.collectionAgreement(true)
			.advertisementAgreement(true)
			.pushAgreement(true)
			.socialType(SocialType.KAKAO)
			.build();

		Trainer trainer = Trainer.builder()
			.member(member)
			.build();

		trainerRepository.save(trainer);

		String invitationCode = "noExistCode";

		// when & then
		mockMvc.perform(get("/trainers/invitation-code/verify/" + invitationCode))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isVerified").value(false));
	}

	@Test
	@DisplayName("통합 테스트 - 연결 완료된 트레이니 최초로 정보 가져오기 성공")
	void get_first_connected_trainee_success() throws Exception {
		// given
		Member trainerMember = MemberFixture.getMember1();
		Member traineeMember = MemberFixture.getMember2();

		trainerMember = memberRepository.save(trainerMember);
		traineeMember = memberRepository.save(traineeMember);

		UserDetails traineeUserDetails = User.builder()
			.username(trainerMember.getId().toString())
			.password("")
			.roles("USER")
			.build();

		Authentication authentication = new UsernamePasswordAuthenticationToken(traineeUserDetails, null,
			authoritiesMapper.mapAuthorities(traineeUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		Trainer trainer = Trainer.builder()
			.member(trainerMember)
			.build();

		Trainee trainee = Trainee.builder()
			.member(traineeMember)
			.height(180.5)
			.weight(78.4)
			.cautionNote("주의사항")
			.build();

		trainer = trainerRepository.save(trainer);
		trainee = traineeRepository.save(trainee);

		PtTrainerTrainee ptTrainerTrainee = PtTrainerTrainee.builder()
			.trainerId(trainer.getId())
			.traineeId(trainee.getId())
			.startedAt(LocalDate.now())
			.finishedPtCount(10)
			.totalPtCount(20)
			.build();

		ptTrainerTraineeRepository.save(ptTrainerTrainee);

		PtGoal ptGoal1 = PtGoal.builder()
			.traineeId(trainee.getId())
			.content("다이어트")
			.build();

		PtGoal ptGoal2 = PtGoal.builder()
			.traineeId(trainee.getId())
			.content("체중 감량")
			.build();

		ptGoalRepository.saveAll(List.of(ptGoal1, ptGoal2));

		// when & then
		mockMvc.perform(get("/trainers/first-connected-trainee")
				.param("trainerId", trainer.getId().toString())
				.param("traineeId", trainee.getId().toString()))
			.andExpect(status().isOk());
	}
}
