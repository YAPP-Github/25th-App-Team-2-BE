package com.tnt.presentation.trainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.tnt.domain.member.Member;
import com.tnt.domain.member.SocialType;
import com.tnt.domain.trainer.Trainer;
import com.tnt.infrastructure.mysql.repository.trainer.TrainerRepository;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class TrainerControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TrainerRepository trainerRepository;

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
}
