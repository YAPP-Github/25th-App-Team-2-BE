package com.tnt.presentation.trainee;

import static com.tnt.domain.trainee.DietType.DINNER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tnt.domain.member.Member;
import com.tnt.domain.trainee.Trainee;
import com.tnt.domain.trainer.Trainer;
import com.tnt.dto.trainee.request.ConnectWithTrainerRequest;
import com.tnt.dto.trainee.request.CreateDietRequest;
import com.tnt.fixture.MemberFixture;
import com.tnt.fixture.TraineeFixture;
import com.tnt.fixture.TrainerFixture;
import com.tnt.gateway.filter.CustomUserDetails;
import com.tnt.infrastructure.mysql.repository.member.MemberRepository;
import com.tnt.infrastructure.mysql.repository.trainee.TraineeRepository;
import com.tnt.infrastructure.mysql.repository.trainer.TrainerRepository;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class TraineeControllerTest {

	private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private TrainerRepository trainerRepository;

	@Autowired
	private TraineeRepository traineeRepository;

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("통합 테스트 - 트레이너, 트레이니 연결 성공")
	void connect_with_trainer_success() throws Exception {
		// given
		Member trainerMember = MemberFixture.getTrainerMember1();
		Member traineeMember = MemberFixture.getTraineeMember1();

		trainerMember = memberRepository.save(trainerMember);
		traineeMember = memberRepository.save(traineeMember);

		CustomUserDetails traineeUserDetails = new CustomUserDetails(traineeMember.getId(),
			traineeMember.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(traineeUserDetails, null,
			authoritiesMapper.mapAuthorities(traineeUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		Trainer trainer = TrainerFixture.getTrainer2(trainerMember);
		Trainee trainee = TraineeFixture.getTrainee1(traineeMember);

		trainerRepository.save(trainer);
		traineeRepository.save(trainee);

		String invitationCode = trainer.getInvitationCode();
		LocalDate startDate = LocalDate.now();
		Integer totalPtCount = 5;
		Integer finishedPtCount = 3;

		ConnectWithTrainerRequest request = new ConnectWithTrainerRequest(invitationCode, startDate, totalPtCount,
			finishedPtCount);

		String json = objectMapper.writeValueAsString(request);

		// when & then
		mockMvc.perform(post("/trainees/connect-trainer")
				.contentType("application/json")
				.content(json))
			.andExpect(status().isCreated());
	}

	@Test
	@DisplayName("통합 테스트 - 식단 등록 성공")
	void create_diet_success() throws Exception {
		// given
		Member trainerMember = MemberFixture.getTrainerMember1();
		Member traineeMember = MemberFixture.getTraineeMember2();

		trainerMember = memberRepository.save(trainerMember);
		traineeMember = memberRepository.save(traineeMember);

		CustomUserDetails traineeUserDetails = new CustomUserDetails(traineeMember.getId(),
			traineeMember.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(traineeUserDetails, null,
			authoritiesMapper.mapAuthorities(traineeUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		Trainer trainer = TrainerFixture.getTrainer2(trainerMember);
		Trainee trainee = TraineeFixture.getTrainee1(traineeMember);

		trainerRepository.save(trainer);
		traineeRepository.save(trainee);

		LocalDateTime date = LocalDateTime.now();
		String memo = "배부르다";

		CreateDietRequest request = new CreateDietRequest(date, DINNER, memo);

		MockMultipartFile dietImage = new MockMultipartFile("dietImage", "test.jpg", IMAGE_JPEG_VALUE,
			"test image content".getBytes());

		// when
		var jsonRequest = new MockMultipartFile("request", "", APPLICATION_JSON_VALUE,
			objectMapper.writeValueAsString(request).getBytes());
		var result = mockMvc.perform(multipart("/trainees/diets")
			.file(jsonRequest)
			.file(dietImage)
			.contentType(MULTIPART_FORM_DATA_VALUE));

		// then
		result.andExpect(status().isCreated())
			.andDo(print());
	}
}
