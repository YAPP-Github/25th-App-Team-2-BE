package com.tnt.presentation.member;

import static com.tnt.common.constant.ProfileConstant.*;
import static com.tnt.domain.member.MemberType.*;
import static com.tnt.domain.member.SocialType.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tnt.domain.member.Member;
import com.tnt.domain.pt.PtTrainerTrainee;
import com.tnt.domain.trainee.PtGoal;
import com.tnt.domain.trainee.Trainee;
import com.tnt.domain.trainer.Trainer;
import com.tnt.dto.member.request.SignUpRequest;
import com.tnt.fixture.MemberFixture;
import com.tnt.fixture.PtGoalsFixture;
import com.tnt.fixture.PtTrainerTraineeFixture;
import com.tnt.fixture.TraineeFixture;
import com.tnt.fixture.TrainerFixture;
import com.tnt.gateway.filter.CustomUserDetails;
import com.tnt.infrastructure.mysql.repository.member.MemberRepository;
import com.tnt.infrastructure.mysql.repository.pt.PtGoalRepository;
import com.tnt.infrastructure.mysql.repository.pt.PtTrainerTraineeRepository;
import com.tnt.infrastructure.mysql.repository.trainee.TraineeRepository;
import com.tnt.infrastructure.mysql.repository.trainer.TrainerRepository;
import com.tnt.infrastructure.redis.AbstractContainerBaseTest;

import reactor.core.publisher.Mono;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Import(MemberControllerTest.TestConfig.class)
class MemberControllerTest extends AbstractContainerBaseTest {

	private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

	private final MockMultipartFile profileImage = new MockMultipartFile("profileImage", "test.jpg",
		IMAGE_JPEG_VALUE, "test image content".getBytes());

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

	@Autowired
	private PtGoalRepository ptGoalRepository;

	@Autowired
	private PtTrainerTraineeRepository ptTrainerTraineeRepository;

	@Test
	@DisplayName("통합 테스트 - 트레이너 회원가입 성공")
	void sign_up_trainer_success() throws Exception {
		// given
		SignUpRequest request = new SignUpRequest("fcm-token-test", TRAINER, KAKAO, "12345", "test@kakao.com", true,
			true, true, "홍길동", LocalDate.of(1990, 1, 1), 175.0, 70.0, "테스트 주의사항", List.of("체중 감량", "근력 향상"));

		// when
		var jsonRequest = new MockMultipartFile("request", "", APPLICATION_JSON_VALUE,
			objectMapper.writeValueAsString(request).getBytes());
		var result = mockMvc.perform(multipart("/members/sign-up")
			.file(jsonRequest)
			.file(profileImage)
			.contentType(MULTIPART_FORM_DATA_VALUE));

		// then
		result.andExpect(status().isCreated())
			.andExpect(jsonPath("$.memberType").value(TRAINER.toString()))
			.andExpect(jsonPath("$.sessionId").exists())
			.andExpect(jsonPath("$.name").value("홍길동"))
			.andExpect(jsonPath("$.profileImageUrl").value(TRAINER_DEFAULT_IMAGE));
	}

	@Test
	@DisplayName("통합 테스트 - 트레이니 회원가입 성공")
	void sign_up_trainee_success() throws Exception {
		// given
		SignUpRequest request = new SignUpRequest("fcm-token-test", TRAINEE, KAKAO, "12345", "test@kakao.com", true,
			true, true, "홍길동", LocalDate.of(1990, 1, 1), 175.0, 70.0, "테스트 주의사항", List.of("체중 감량", "근력 향상"));

		// when
		var jsonRequest = new MockMultipartFile("request", "", APPLICATION_JSON_VALUE,
			objectMapper.writeValueAsString(request).getBytes());
		var result = mockMvc.perform(multipart("/members/sign-up")
			.file(jsonRequest)
			.file(profileImage)
			.contentType(MULTIPART_FORM_DATA_VALUE));

		// then
		result.andExpect(status().isCreated())
			.andExpect(jsonPath("$.memberType").value(TRAINEE.toString()))
			.andExpect(jsonPath("$.sessionId").exists())
			.andExpect(jsonPath("$.name").value("홍길동"))
			.andExpect(jsonPath("$.profileImageUrl").value(TRAINEE_DEFAULT_IMAGE));
	}

	@Test
	@DisplayName("통합 테스트 - 필수 필드 누락으로 회원가입 실패")
	void sign_up_missing_required_field_fail() throws Exception {
		// given
		SignUpRequest request = new SignUpRequest("", TRAINER, KAKAO, "12345", "test@kakao.com", true,
			true, true, "홍길동", LocalDate.of(1990, 1, 1), 175.0, 70.0, "테스트 주의사항", List.of("체중 감량", "근력 향상"));

		// when
		var jsonRequest = new MockMultipartFile("request", "", APPLICATION_JSON_VALUE,
			objectMapper.writeValueAsString(request).getBytes());
		var result = mockMvc.perform(multipart("/members/sign-up")
			.file(jsonRequest)
			.file(profileImage)
			.contentType(MULTIPART_FORM_DATA_VALUE));

		// then
		result.andExpect(status().is4xxClientError());
	}

	@Test
	@DisplayName("통합 테스트 - 트레이너 탈퇴 성공")
	void revoke_trainer_success() throws Exception {
		// given
		Member trainerMember = MemberFixture.getTrainerMember1();

		Member member = memberRepository.save(trainerMember);

		CustomUserDetails traineeUserDetails = new CustomUserDetails(member.getId(),
			String.valueOf(member.getId()), List.of(new SimpleGrantedAuthority("ROLE_USER")));

		Authentication authentication = new UsernamePasswordAuthenticationToken(traineeUserDetails, null,
			authoritiesMapper.mapAuthorities(traineeUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		Trainer trainer = TrainerFixture.getTrainer2(trainerMember);

		trainerRepository.save(trainer);

		// when & then
		mockMvc.perform(post("/members/withdraw")
				.contentType(APPLICATION_JSON_VALUE))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("통합 테스트 - 트레이니 탈퇴 성공")
	void revoke_trainee_success() throws Exception {
		// given
		Member traineeMember = MemberFixture.getTraineeMember1();

		Member member = memberRepository.save(traineeMember);

		CustomUserDetails traineeUserDetails = new CustomUserDetails(member.getId(),
			String.valueOf(member.getId()), List.of(new SimpleGrantedAuthority("ROLE_USER")));

		Authentication authentication = new UsernamePasswordAuthenticationToken(traineeUserDetails, null,
			authoritiesMapper.mapAuthorities(traineeUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		Trainee trainee = TraineeFixture.getTrainee2(traineeMember);

		traineeRepository.save(trainee);

		// when & then
		mockMvc.perform(post("/members/withdraw")
				.contentType(APPLICATION_JSON_VALUE))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("통합 테스트 - 트레이너 회원 조회 성공")
	void get_member_info_trainee_success() throws Exception {
		// given
		Member trainerMember = MemberFixture.getTrainerMember1();
		Member traineeMember = MemberFixture.getTraineeMember1();

		Member member1 = memberRepository.save(trainerMember);
		memberRepository.save(traineeMember);

		CustomUserDetails traineeUserDetails = new CustomUserDetails(member1.getId(),
			String.valueOf(member1.getId()), List.of(new SimpleGrantedAuthority("ROLE_USER")));

		Authentication authentication = new UsernamePasswordAuthenticationToken(traineeUserDetails, null,
			authoritiesMapper.mapAuthorities(traineeUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		Trainer trainer = TrainerFixture.getTrainer2(trainerMember);
		Trainee trainee = TraineeFixture.getTrainee2(traineeMember);

		trainerRepository.save(trainer);
		traineeRepository.save(trainee);

		List<PtGoal> ptGoals = PtGoalsFixture.getPtGoals(trainee.getId());

		ptGoalRepository.saveAll(ptGoals);

		PtTrainerTrainee ptTrainerTrainee = PtTrainerTraineeFixture.getPtTrainerTrainee1(trainer, trainee);

		ptTrainerTraineeRepository.save(ptTrainerTrainee);

		// when & then
		mockMvc.perform(get("/members")
				.contentType(APPLICATION_JSON_VALUE))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value(member1.getName()))
			.andExpect(jsonPath("$.email").value(member1.getEmail()))
			.andExpect(jsonPath("$.profileImageUrl").value(member1.getProfileImageUrl()))
			.andExpect(jsonPath("$.birthday").value(member1.getBirthday().toString()))
			.andExpect(jsonPath("$.memberType").value(member1.getMemberType().name()))
			.andExpect(jsonPath("$.socialType").value(member1.getSocialType().name()))
			.andExpect(jsonPath("$.activeTraineeCount").exists())
			.andExpect(jsonPath("$.previousTraineeCount").exists());
	}

	@TestConfiguration
	static class TestConfig {

		@Bean
		@Primary
		public WebClient mockWebClient() {
			return WebClient.builder()
				.exchangeFunction(clientRequest -> {
					if (clientRequest.url().toString().contains("unlink")) {
						return Mono.just(ClientResponse.create(HttpStatus.OK)
							.header("content-type", "application/json")
							.body("{\"id\":123456789}")
							.build());
					}
					return Mono.just(ClientResponse.create(HttpStatus.OK).build());
				})
				.build();
		}
	}
}
