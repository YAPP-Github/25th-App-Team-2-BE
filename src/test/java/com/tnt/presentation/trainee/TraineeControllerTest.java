package com.tnt.presentation.trainee;

import static com.tnt.domain.trainee.DietType.BREAKFAST;
import static com.tnt.domain.trainee.DietType.DINNER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import com.tnt.domain.pt.PtLesson;
import com.tnt.domain.pt.PtTrainerTrainee;
import com.tnt.domain.trainee.Diet;
import com.tnt.domain.trainee.Trainee;
import com.tnt.domain.trainer.Trainer;
import com.tnt.dto.trainee.request.ConnectWithTrainerRequest;
import com.tnt.dto.trainee.request.CreateDietRequest;
import com.tnt.fixture.DietFixture;
import com.tnt.fixture.MemberFixture;
import com.tnt.fixture.PtLessonsFixture;
import com.tnt.fixture.PtTrainerTraineeFixture;
import com.tnt.fixture.TraineeFixture;
import com.tnt.fixture.TrainerFixture;
import com.tnt.gateway.filter.CustomUserDetails;
import com.tnt.infrastructure.mysql.repository.member.MemberRepository;
import com.tnt.infrastructure.mysql.repository.pt.PtLessonRepository;
import com.tnt.infrastructure.mysql.repository.pt.PtTrainerTraineeRepository;
import com.tnt.infrastructure.mysql.repository.trainee.DietRepository;
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

	@Autowired
	private DietRepository dietRepository;

	@Autowired
	private PtTrainerTraineeRepository ptTrainerTraineeRepository;

	@Autowired
	private PtLessonRepository ptLessonRepository;

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
	@DisplayName("통합 테스트 - 트레이니 식단 등록 성공")
	void create_diet_success() throws Exception {
		// given
		Member traineeMember = MemberFixture.getTraineeMember2();

		traineeMember = memberRepository.save(traineeMember);

		CustomUserDetails traineeUserDetails = new CustomUserDetails(traineeMember.getId(),
			traineeMember.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(traineeUserDetails, null,
			authoritiesMapper.mapAuthorities(traineeUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		Trainee trainee = TraineeFixture.getTrainee1(traineeMember);

		traineeRepository.save(trainee);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		LocalDateTime date = LocalDateTime.parse("2025-02-11T15:38");
		String formattedDate = date.format(formatter);
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
			.andExpect(jsonPath("$.date").value(formattedDate))
			.andExpect(jsonPath("$.dietImageUrl").doesNotExist())
			.andExpect(jsonPath("$.memo").value(memo))
			.andExpect(jsonPath("$.dietType").value(DINNER.toString()))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 트레이니 사진 없이 식단 등록 성공")
	void create_diet_without_image_success() throws Exception {
		// given
		Member traineeMember = MemberFixture.getTraineeMember2();

		traineeMember = memberRepository.save(traineeMember);

		CustomUserDetails traineeUserDetails = new CustomUserDetails(traineeMember.getId(),
			traineeMember.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(traineeUserDetails, null,
			authoritiesMapper.mapAuthorities(traineeUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		Trainee trainee = TraineeFixture.getTrainee1(traineeMember);

		traineeRepository.save(trainee);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		LocalDateTime date = LocalDateTime.parse("2025-02-11T15:38");
		String formattedDate = date.format(formatter);
		String memo = "배부르다";

		CreateDietRequest request = new CreateDietRequest(date, DINNER, memo);

		// when
		var jsonRequest = new MockMultipartFile("request", "", APPLICATION_JSON_VALUE,
			objectMapper.writeValueAsString(request).getBytes());
		var result = mockMvc.perform(multipart("/trainees/diets")
			.file(jsonRequest)
			.contentType(MULTIPART_FORM_DATA_VALUE));

		// then
		result.andExpect(status().isCreated())
			.andExpect(jsonPath("$.date").value(formattedDate))
			.andExpect(jsonPath("$.dietImageUrl").doesNotExist())
			.andExpect(jsonPath("$.memo").value(memo))
			.andExpect(jsonPath("$.dietType").value(DINNER.toString()))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 트레이니 중복 시간대 식단 등록 실패")
	void create_diet_duplicate_time_fail() throws Exception {
		// given
		Member traineeMember = MemberFixture.getTraineeMember2();

		traineeMember = memberRepository.save(traineeMember);

		CustomUserDetails traineeUserDetails = new CustomUserDetails(traineeMember.getId(),
			traineeMember.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(traineeUserDetails, null,
			authoritiesMapper.mapAuthorities(traineeUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		Trainee trainee = TraineeFixture.getTrainee1(traineeMember);

		traineeRepository.save(trainee);

		Diet diet = DietFixture.getDiet1(trainee.getId());

		dietRepository.save(diet);

		LocalDateTime date = LocalDateTime.parse("2025-02-01T11:38");
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
		result.andExpect(status().isConflict())
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 트레이니 특정 식단 조회 성공")
	void get_diet_with_diet_id_success() throws Exception {
		// given
		Member traineeMember = MemberFixture.getTraineeMember2();

		traineeMember = memberRepository.save(traineeMember);

		CustomUserDetails traineeUserDetails = new CustomUserDetails(traineeMember.getId(),
			traineeMember.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(traineeUserDetails, null,
			authoritiesMapper.mapAuthorities(traineeUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		Trainee trainee = TraineeFixture.getTrainee1(traineeMember);

		traineeRepository.save(trainee);

		Diet diet = DietFixture.getDiet1(trainee.getId());

		dietRepository.save(diet);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String formattedDate = diet.getDate().format(formatter);

		// when
		mockMvc.perform(get("/trainees/diets/{dietId}", diet.getId())
				.contentType("application/json"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.dietId").value(diet.getId()))
			.andExpect(jsonPath("$.date").value(formattedDate))
			.andExpect(jsonPath("$.dietImageUrl").value(diet.getDietImageUrl()))
			.andExpect(jsonPath("$.memo").value(diet.getMemo()))
			.andExpect(jsonPath("$.dietType").value(BREAKFAST.toString()))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 트레이니 특정 식단 조회 실패")
	void get_diet_with_diet_id_failure() throws Exception {
		// given
		Member traineeMember = MemberFixture.getTraineeMember2();

		traineeMember = memberRepository.save(traineeMember);

		CustomUserDetails traineeUserDetails = new CustomUserDetails(traineeMember.getId(),
			traineeMember.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(traineeUserDetails, null,
			authoritiesMapper.mapAuthorities(traineeUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		Trainee trainee = TraineeFixture.getTrainee1(traineeMember);

		traineeRepository.save(trainee);

		// when
		mockMvc.perform(get("/trainees/diets/{dietId}", 123)
				.contentType("application/json"))
			.andExpect(status().is4xxClientError())
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 트레이니 캘린더 PT 수업, 기록 있는 날짜 조회 성공")
	void get_trainee_calendar_pt_lesson_count_success() throws Exception {
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

		trainer = trainerRepository.save(trainer);
		trainee = traineeRepository.save(trainee);

		PtTrainerTrainee ptTrainerTrainee = PtTrainerTraineeFixture.getPtTrainerTrainee1(trainer, trainee);

		ptTrainerTrainee = ptTrainerTraineeRepository.save(ptTrainerTrainee);

		LocalDateTime date1 = LocalDateTime.of(2025, 1, 3, 10, 0);
		LocalDateTime date2 = LocalDateTime.of(2025, 1, 5, 13, 20);
		LocalDateTime date3 = LocalDateTime.of(2025, 1, 7, 20, 0);
		LocalDateTime date4 = LocalDateTime.of(2025, 1, 10, 17, 30);

		List<PtLesson> ptLessons = List.of(PtLesson.builder()
				.ptTrainerTrainee(ptTrainerTrainee)
				.session(1)
				.lessonStart(date1)
				.lessonEnd(date1.plusHours(1))
				.build(),
			PtLesson.builder()
				.ptTrainerTrainee(ptTrainerTrainee)
				.session(2)
				.lessonStart(date2)
				.lessonEnd(date2.plusHours(1))
				.build(),
			PtLesson.builder()
				.ptTrainerTrainee(ptTrainerTrainee)
				.session(3)
				.lessonStart(date3)
				.lessonEnd(date3.plusHours(1))
				.build(),
			PtLesson.builder()
				.ptTrainerTrainee(ptTrainerTrainee)
				.session(4)
				.lessonStart(date4)
				.lessonEnd(date4.plusHours(1))
				.build());

		ptLessonRepository.saveAll(ptLessons);

		Diet diet1 = DietFixture.getDiet1(trainee.getId());
		Diet diet2 = DietFixture.getDiet2(trainee.getId());

		dietRepository.saveAll(List.of(diet1, diet2));

		LocalDate startDate = LocalDate.of(2024, 12, 1);
		LocalDate endDate = LocalDate.of(2025, 2, 28);

		// when & then
		mockMvc.perform(get("/trainees/lessons/calendar")
				.param("startDate", startDate.toString())
				.param("endDate", endDate.toString())
				.contentType("application/json"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.ptLessonDates").isArray())
			.andExpect(jsonPath("$.ptLessonDates.size()").value(5))
			.andExpect(jsonPath("$.ptLessonDates[0]").value("2025-01-03"))
			.andExpect(jsonPath("$.ptLessonDates[1]").value("2025-01-05"))
			.andExpect(jsonPath("$.ptLessonDates[2]").value("2025-01-07"))
			.andExpect(jsonPath("$.ptLessonDates[3]").value("2025-01-10"))
			.andExpect(jsonPath("$.ptLessonDates[4]").value(diet1.getDate().toLocalDate().toString()))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 트레이니 특정 날짜 기록 조회 성공")
	void get_calendar_daily_records_success() throws Exception {
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

		PtTrainerTrainee ptTrainerTrainee = PtTrainerTraineeFixture.getPtTrainerTrainee1(trainer, trainee);

		ptTrainerTraineeRepository.save(ptTrainerTrainee);

		PtLesson ptLesson = PtLessonsFixture.getPtLessons1(ptTrainerTrainee).getFirst();

		ptLessonRepository.save(ptLesson);

		Diet diet1 = DietFixture.getDiet1(trainee.getId());
		Diet diet2 = DietFixture.getDiet2(trainee.getId());

		List<Diet> diets = List.of(diet1, diet2);

		dietRepository.saveAll(diets);

		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		LocalDate targetDate = diet1.getDate().toLocalDate();

		// when & then
		mockMvc.perform(get("/trainees/calendar/{date}", targetDate))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.date").value(targetDate.format(dateFormatter)))
			.andExpect(jsonPath("$.ptInfo.trainerName").value(trainer.getMember().getName()))
			.andExpect(jsonPath("$.ptInfo.trainerProfileImage").value(trainer.getMember().getProfileImageUrl()))
			.andExpect(jsonPath("$.ptInfo.session").value(ptLesson.getSession()))
			.andExpect(jsonPath("$.ptInfo.lessonStart").value(ptLesson.getLessonStart().format(dateTimeFormatter)))
			.andExpect(jsonPath("$.ptInfo.lessonEnd").value(ptLesson.getLessonEnd().format(dateTimeFormatter)))
			.andExpect(jsonPath("$.diets").isArray())
			.andExpect(jsonPath("$.diets[0].dietId").value(diet1.getId()))
			.andExpect(jsonPath("$.diets[0].date").value(diet1.getDate().format(dateTimeFormatter)))
			.andExpect(jsonPath("$.diets[0].dietImageUrl").value(diet1.getDietImageUrl()))
			.andExpect(jsonPath("$.diets[0].memo").value(diet1.getMemo()))
			.andExpect(jsonPath("$.diets[0].dietType").value(diet1.getDietType().toString()))
			.andExpect(jsonPath("$.diets[1].dietId").value(diet2.getId()))
			.andExpect(jsonPath("$.diets[1].date").value(diet2.getDate().format(dateTimeFormatter)))
			.andExpect(jsonPath("$.diets[1].dietImageUrl").value(diet2.getDietImageUrl()))
			.andExpect(jsonPath("$.diets[1].memo").value(diet2.getMemo()))
			.andExpect(jsonPath("$.diets[1].dietType").value(diet2.getDietType().toString()))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 트레이니 PT 수업 없는 날 특정 날짜 기록 조회 성공")
	void get_calendar_daily_records_without_pt_info_success() throws Exception {
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

		PtTrainerTrainee ptTrainerTrainee = PtTrainerTraineeFixture.getPtTrainerTrainee1(trainer, trainee);

		ptTrainerTraineeRepository.save(ptTrainerTrainee);

		PtLesson ptLesson = PtLessonsFixture.getPtLessons1(ptTrainerTrainee).getFirst();

		ptLessonRepository.save(ptLesson);

		Diet diet3 = DietFixture.getDiet3(trainee.getId());
		Diet diet4 = DietFixture.getDiet4(trainee.getId());

		List<Diet> diets = List.of(diet3, diet4);

		dietRepository.saveAll(diets);

		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		LocalDate targetDate = diet3.getDate().toLocalDate();

		// when & then
		mockMvc.perform(get("/trainees/calendar/{date}", targetDate))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.date").value(targetDate.format(dateFormatter)))
			.andExpect(jsonPath("$.ptInfo").doesNotExist())
			.andExpect(jsonPath("$.diets").isArray())
			.andExpect(jsonPath("$.diets[0].dietId").value(diet3.getId()))
			.andExpect(jsonPath("$.diets[0].date").value(diet3.getDate().format(dateTimeFormatter)))
			.andExpect(jsonPath("$.diets[0].dietImageUrl").value(diet3.getDietImageUrl()))
			.andExpect(jsonPath("$.diets[0].memo").value(diet3.getMemo()))
			.andExpect(jsonPath("$.diets[0].dietType").value(diet3.getDietType().toString()))
			.andExpect(jsonPath("$.diets[1].dietId").value(diet4.getId()))
			.andExpect(jsonPath("$.diets[1].date").value(diet4.getDate().format(dateTimeFormatter)))
			.andExpect(jsonPath("$.diets[1].dietImageUrl").value(diet4.getDietImageUrl()))
			.andExpect(jsonPath("$.diets[1].memo").value(diet4.getMemo()))
			.andExpect(jsonPath("$.diets[1].dietType").value(diet4.getDietType().toString()))
			.andDo(print());
	}
}
