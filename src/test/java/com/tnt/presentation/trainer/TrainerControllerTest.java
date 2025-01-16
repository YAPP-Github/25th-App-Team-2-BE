package com.tnt.presentation.trainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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

		Trainer trainer = Trainer.builder()
			.memberId(memberId)
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

		Trainer trainer = Trainer.builder()
			.memberId(memberId)
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

		Trainer trainer = Trainer.builder()
			.memberId(memberId)
			.build();

		trainerRepository.save(trainer);

		// when & then
		mockMvc.perform(put("/trainers/invitation-code")).andExpect(status().isCreated());
	}
}
