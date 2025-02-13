package com.tnt.presentation.trainer;

import static com.tnt.domain.member.MemberType.TRAINER;
import static com.tnt.domain.member.SocialType.KAKAO;
import static java.util.Comparator.comparing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tnt.annotation.WithMockCustomUser;
import com.tnt.domain.member.Member;
import com.tnt.domain.pt.PtLesson;
import com.tnt.domain.pt.PtTrainerTrainee;
import com.tnt.domain.trainee.PtGoal;
import com.tnt.domain.trainee.Trainee;
import com.tnt.domain.trainer.Trainer;
import com.tnt.dto.trainer.request.CreatePtLessonRequest;
import com.tnt.fixture.MemberFixture;
import com.tnt.fixture.PtGoalsFixture;
import com.tnt.fixture.PtTrainerTraineeFixture;
import com.tnt.fixture.TraineeFixture;
import com.tnt.fixture.TrainerFixture;
import com.tnt.gateway.filter.CustomUserDetails;
import com.tnt.infrastructure.mysql.repository.member.MemberRepository;
import com.tnt.infrastructure.mysql.repository.pt.PtGoalRepository;
import com.tnt.infrastructure.mysql.repository.pt.PtLessonRepository;
import com.tnt.infrastructure.mysql.repository.pt.PtTrainerTraineeRepository;
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

	@Autowired
	private PtLessonRepository ptLessonRepository;

	@Test
	@DisplayName("통합 테스트 - 트레이너 초대 코드 불러오기 성공")
	@WithMockCustomUser(memberId = 1L)
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
			.socialType(KAKAO)
			.memberType(TRAINER)
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
	@WithMockCustomUser(memberId = 2L)
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
			.socialType(KAKAO)
			.memberType(TRAINER)
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
	@WithMockCustomUser(memberId = 3L)
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
			.socialType(KAKAO)
			.memberType(TRAINER)
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
		String socialId = "1234567890";
		String email = "abc@gmail.com";
		String name = "김영명";
		String profileImageUrl = "https://profile.com/1234567890";

		Member member = Member.builder()
			.socialId(socialId)
			.email(email)
			.name(name)
			.fcmToken("fcmToken")
			.profileImageUrl(profileImageUrl)
			.serviceAgreement(true)
			.collectionAgreement(true)
			.advertisementAgreement(true)
			.socialType(KAKAO)
			.memberType(TRAINER)
			.build();

		Trainer trainer = Trainer.builder()
			.member(member)
			.build();

		memberRepository.save(member);
		trainerRepository.save(trainer);

		String invitationCode = trainer.getInvitationCode();

		// when & then
		mockMvc.perform(get("/trainers/invitation-code/verify/" + invitationCode))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isVerified").value(true))
			.andExpect(jsonPath("$.trainerName").value(name));
	}

	@Test
	@DisplayName("통합 테스트 - 트레이너 초대 코드 인증 실패")
	void verify_invitation_code_fail() throws Exception {
		// given
		String socialId = "1234567890";
		String email = "abc@gmail.com";
		String name = "김영명";
		String profileImageUrl = "https://profile.com/1234567890";

		Member member = Member.builder()
			.socialId(socialId)
			.email(email)
			.name(name)
			.fcmToken("fcmToken")
			.profileImageUrl(profileImageUrl)
			.serviceAgreement(true)
			.collectionAgreement(true)
			.advertisementAgreement(true)
			.socialType(KAKAO)
			.memberType(TRAINER)
			.build();

		Trainer trainer = Trainer.builder()
			.member(member)
			.build();

		memberRepository.save(member);
		trainerRepository.save(trainer);

		String invitationCode = "noExistCode";

		// when & then
		mockMvc.perform(get("/trainers/invitation-code/verify/" + invitationCode))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isVerified").value(false))
			.andExpect(jsonPath("$.trainerName").doesNotExist());
	}

	@Test
	@DisplayName("통합 테스트 - 연결 완료된 트레이니 최초로 정보 가져오기 성공")
	void get_first_connected_trainee_success() throws Exception {
		// given
		Member trainerMember = MemberFixture.getTrainerMember1();
		Member traineeMember = MemberFixture.getTraineeMember1();

		trainerMember = memberRepository.save(trainerMember);
		traineeMember = memberRepository.save(traineeMember);

		CustomUserDetails trainerUserDetails = new CustomUserDetails(trainerMember.getId(),
			trainerMember.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(trainerUserDetails, null,
			authoritiesMapper.mapAuthorities(trainerUserDetails.getAuthorities()));

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

		PtTrainerTrainee ptTrainerTrainee = PtTrainerTraineeFixture.getPtTrainerTrainee1(trainer, trainee);

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
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.trainer.trainerName").value(trainerMember.getName()))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 특정 날짜 PT 리스트 불러오기 성공")
	void get_pt_lessons_on_date_success() throws Exception {
		// given
		Member trainerMember = MemberFixture.getTrainerMember1();
		Member traineeMember = MemberFixture.getTraineeMember1();

		trainerMember = memberRepository.save(trainerMember);
		traineeMember = memberRepository.save(traineeMember);

		CustomUserDetails trainerUserDetails = new CustomUserDetails(trainerMember.getId(),
			trainerMember.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(trainerUserDetails, null,
			authoritiesMapper.mapAuthorities(trainerUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		Trainer trainer = TrainerFixture.getTrainer2(trainerMember);
		Trainee trainee = TraineeFixture.getTrainee1(traineeMember);
		trainer = trainerRepository.save(trainer);
		trainee = traineeRepository.save(trainee);

		PtTrainerTrainee ptTrainerTrainee = PtTrainerTraineeFixture.getPtTrainerTrainee1(trainer, trainee);
		ptTrainerTraineeRepository.save(ptTrainerTrainee);

		PtLesson ptLesson = PtLesson.builder()
			.ptTrainerTrainee(ptTrainerTrainee)
			.lessonStart(LocalDateTime.of(2025, 1, 1, 10, 0))
			.lessonEnd(LocalDateTime.of(2025, 1, 1, 11, 0))
			.session(1)
			.memo("THIS IS MEMO")
			.build();

		ptLesson = ptLessonRepository.save(ptLesson);

		// when & then
		mockMvc.perform(get("/trainers/lessons/{date}", "2025-01-01"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.count").value(1))
			.andExpect(jsonPath("$.date").value("2025-01-01"))
			.andExpect(jsonPath("$.lessons[0].ptLessonId").value(ptLesson.getId()));
	}

	@Test
	@DisplayName("통합 테스트 - 특정 월의 캘린더 PT 레슨 수 조회 성공")
	void get_calendar_pt_lesson_count_success() throws Exception {
		Member trainerMember = MemberFixture.getTrainerMember1();
		Member traineeMember = MemberFixture.getTraineeMember1();

		trainerMember = memberRepository.save(trainerMember);
		traineeMember = memberRepository.save(traineeMember);

		CustomUserDetails trainerUserDetails = new CustomUserDetails(trainerMember.getId(),
			trainerMember.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(trainerUserDetails, null,
			authoritiesMapper.mapAuthorities(trainerUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		Trainer trainer = TrainerFixture.getTrainer2(trainerMember);
		Trainee trainee = TraineeFixture.getTrainee1(traineeMember);
		trainer = trainerRepository.save(trainer);
		trainee = traineeRepository.save(trainee);

		PtTrainerTrainee ptTrainerTrainee = PtTrainerTraineeFixture.getPtTrainerTrainee1(trainer, trainee);
		ptTrainerTraineeRepository.save(ptTrainerTrainee);

		int year = 2025;
		int month = 1;
		LocalDateTime date = LocalDate.of(year, month, 1).atTime(10, 0);

		List<PtLesson> ptLessons = List.of(PtLesson.builder()
				.ptTrainerTrainee(ptTrainerTrainee)
				.session(1)
				.lessonStart(date)
				.lessonEnd(date.plusHours(1))
				.build(),
			PtLesson.builder()
				.ptTrainerTrainee(ptTrainerTrainee)
				.session(2)
				.lessonStart(date.plusHours(4))
				.lessonEnd(date.plusHours(5))
				.build(),
			PtLesson.builder()
				.ptTrainerTrainee(ptTrainerTrainee)
				.session(3)
				.lessonStart(date.plusDays(1))
				.lessonEnd(date.plusDays(1).plusHours(1))
				.build(),
			PtLesson.builder()
				.ptTrainerTrainee(ptTrainerTrainee)
				.session(4)
				.lessonStart(date.plusDays(4).plusHours(4))
				.lessonEnd(date.plusDays(4).plusHours(5))
				.build());

		ptLessonRepository.saveAll(ptLessons);

		// when & then
		mockMvc.perform(get("/trainers/lessons/calendar")
				.param("year", String.valueOf(year))
				.param("month", String.valueOf(month)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.calendarPtLessonCounts").isArray())
			.andExpect(jsonPath("$.calendarPtLessonCounts[0].date").value("2025-01-01"))
			.andExpect(jsonPath("$.calendarPtLessonCounts[0].count").value(2))
			.andExpect(jsonPath("$.calendarPtLessonCounts[1].date").value("2025-01-02"))
			.andExpect(jsonPath("$.calendarPtLessonCounts[1].count").value(1))
			.andExpect(jsonPath("$.calendarPtLessonCounts[2].date").value("2025-01-05"))
			.andExpect(jsonPath("$.calendarPtLessonCounts[2].count").value(1))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 관리 중인 회원 목록 조회 성공")
	void get_active_trainees_success() throws Exception {
		// given
		Member trainerMember = MemberFixture.getTrainerMember1();
		Member traineeMember1 = MemberFixture.getTraineeMember1();
		Member traineeMember2 = MemberFixture.getTraineeMember2();

		trainerMember = memberRepository.save(trainerMember);
		traineeMember1 = memberRepository.save(traineeMember1);
		traineeMember2 = memberRepository.save(traineeMember2);

		CustomUserDetails trainerUserDetails = new CustomUserDetails(trainerMember.getId(),
			trainerMember.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(trainerUserDetails, null,
			authoritiesMapper.mapAuthorities(trainerUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		Trainer trainer = TrainerFixture.getTrainer2(trainerMember);
		Trainee trainee1 = TraineeFixture.getTrainee1(traineeMember1);
		Trainee trainee2 = TraineeFixture.getTrainee2(traineeMember2);

		trainer = trainerRepository.save(trainer);
		trainee1 = traineeRepository.save(trainee1);
		trainee2 = traineeRepository.save(trainee2);

		List<PtGoal> ptGoals1 = PtGoalsFixture.getPtGoals(trainee1.getId());
		List<PtGoal> ptGoals2 = PtGoalsFixture.getPtGoals(trainee2.getId());

		ptGoalRepository.saveAll(ptGoals1);
		ptGoalRepository.saveAll(ptGoals2);

		PtTrainerTrainee ptTrainerTrainee1 = PtTrainerTraineeFixture.getPtTrainerTrainee1(trainer, trainee1);
		PtTrainerTrainee ptTrainerTrainee2 = PtTrainerTraineeFixture.getPtTrainerTrainee2(trainer, trainee2);

		ptTrainerTraineeRepository.save(ptTrainerTrainee1);
		ptTrainerTraineeRepository.save(ptTrainerTrainee2);

		// when & then
		mockMvc.perform(get("/trainers/active-trainees"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.trainees").isArray())
			.andExpect(jsonPath("$.trainees[0].id").value(trainee1.getId()))
			.andExpect(jsonPath("$.trainees[0].name").value(traineeMember1.getName()))
			.andExpect(jsonPath("$.trainees[0].profileImageUrl").value(traineeMember1.getProfileImageUrl()))
			.andExpect(jsonPath("$.trainees[0].finishedPtCount").value(ptTrainerTrainee1.getFinishedPtCount()))
			.andExpect(jsonPath("$.trainees[0].totalPtCount").value(ptTrainerTrainee1.getTotalPtCount()))
			.andExpect(jsonPath("$.trainees[0].memo").value(""))
			.andExpect(jsonPath("$.trainees[1].id").value(trainee2.getId()))
			.andExpect(jsonPath("$.trainees[1].name").value(traineeMember2.getName()))
			.andExpect(jsonPath("$.trainees[1].profileImageUrl").value(traineeMember2.getProfileImageUrl()))
			.andExpect(jsonPath("$.trainees[1].finishedPtCount").value(ptTrainerTrainee2.getFinishedPtCount()))
			.andExpect(jsonPath("$.trainees[1].totalPtCount").value(ptTrainerTrainee2.getTotalPtCount()))
			.andExpect(jsonPath("$.trainees[1].memo").value(""))
			.andDo(print());
	}

	@Test
	@DisplayName("통합 테스트 - 예약된 PT 수업이 하나도 없을 경우 PT 수업 추가 성공")
	void add_pt_lesson_success1() throws Exception {
		// given
		Member trainerMember = MemberFixture.getTrainerMember1();
		Member traineeMember = MemberFixture.getTraineeMember1();

		trainerMember = memberRepository.save(trainerMember);
		traineeMember = memberRepository.save(traineeMember);

		CustomUserDetails trainerUserDetails = new CustomUserDetails(trainerMember.getId(),
			trainerMember.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(trainerUserDetails, null,
			authoritiesMapper.mapAuthorities(trainerUserDetails.getAuthorities()));

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

		PtTrainerTrainee ptTrainerTrainee = PtTrainerTraineeFixture.getPtTrainerTrainee1(trainer, trainee);

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

		LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0);
		LocalDateTime end = LocalDateTime.of(2025, 1, 1, 11, 0);
		String memo = "THIS IS MEMO";

		CreatePtLessonRequest request = new CreatePtLessonRequest(start, end, memo, trainee.getId());

		// when & then
		mockMvc.perform(post("/trainers/lessons")
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		List<PtLesson> ptLessons = ptLessonRepository.findAll();
		assertThat(ptLessons).hasSize(1);
		assertThat(ptLessons.getFirst().getLessonStart()).isEqualTo(start);
		assertThat(ptLessons.getFirst().getLessonEnd()).isEqualTo(end);
		assertThat(ptLessons.getFirst().getMemo()).isEqualTo(memo);
		assertThat(ptLessons.getFirst().getPtTrainerTrainee()).isEqualTo(ptTrainerTrainee);
	}

	@Test
	@DisplayName("통합 테스트 - 예약된 PT 수업들 보다 날짜가 빠른 PT 수업 추가 성공")
	void add_pt_lesson_success2() throws Exception {
		// given
		Member trainerMember = MemberFixture.getTrainerMember1();
		Member traineeMember = MemberFixture.getTraineeMember1();

		trainerMember = memberRepository.save(trainerMember);
		traineeMember = memberRepository.save(traineeMember);

		CustomUserDetails trainerUserDetails = new CustomUserDetails(trainerMember.getId(),
			trainerMember.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(trainerUserDetails, null,
			authoritiesMapper.mapAuthorities(trainerUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		Trainer trainer = TrainerFixture.getTrainer2(trainerMember);
		Trainee trainee = TraineeFixture.getTrainee2(traineeMember);

		trainer = trainerRepository.save(trainer);
		trainee = traineeRepository.save(trainee);

		PtTrainerTrainee ptTrainerTrainee = PtTrainerTraineeFixture.getPtTrainerTrainee1(trainer, trainee);

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

		LocalDateTime startDate1 = LocalDateTime.parse("2025-02-01T11:30");
		LocalDateTime endDate1 = LocalDateTime.parse("2025-02-01T13:00");

		LocalDateTime startDate2 = LocalDateTime.parse("2025-02-02T11:30");
		LocalDateTime endDate2 = LocalDateTime.parse("2025-02-02T13:00");

		List<PtLesson> ptLessons = List.of(PtLesson.builder()
				.ptTrainerTrainee(ptTrainerTrainee)
				.session(4)
				.lessonStart(startDate1)
				.lessonEnd(endDate1)
				.build(),
			PtLesson.builder()
				.ptTrainerTrainee(ptTrainerTrainee)
				.session(5)
				.lessonStart(startDate2)
				.lessonEnd(endDate2)
				.build());

		ptLessonRepository.saveAll(ptLessons);

		LocalDateTime start = LocalDateTime.of(2025, 1, 5, 10, 0);
		LocalDateTime end = LocalDateTime.of(2025, 1, 5, 11, 0);
		String memo = "THIS IS MEMO";

		CreatePtLessonRequest request = new CreatePtLessonRequest(start, end, memo, trainee.getId());

		// when & then
		mockMvc.perform(post("/trainers/lessons")
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andDo(print());

		List<PtLesson> ptLessonsResult = ptLessonRepository.findAll().stream()
			.sorted(comparing(PtLesson::getLessonStart))
			.toList();

		assertThat(ptLessonsResult).hasSize(3);
		assertThat(ptLessonsResult.getFirst().getLessonStart()).isEqualTo(start);
		assertThat(ptLessonsResult.getFirst().getLessonEnd()).isEqualTo(end);
		assertThat(ptLessonsResult.getFirst().getSession()).isEqualTo(4);
		assertThat(ptLessonsResult.getFirst().getMemo()).isEqualTo(memo);
		assertThat(ptLessonsResult.getFirst().getPtTrainerTrainee()).isEqualTo(ptTrainerTrainee);
		assertThat(ptLessonsResult.get(1).getLessonStart()).isEqualTo(ptLessons.getFirst().getLessonStart());
		assertThat(ptLessonsResult.get(1).getLessonEnd()).isEqualTo(ptLessons.getFirst().getLessonEnd());
		assertThat(ptLessonsResult.get(1).getSession()).isEqualTo(5);
		assertThat(ptLessonsResult.get(1).getMemo()).isEqualTo(ptLessons.getFirst().getMemo());
		assertThat(ptLessonsResult.get(1).getPtTrainerTrainee()).isEqualTo(ptTrainerTrainee);
		assertThat(ptLessonsResult.getLast().getLessonStart()).isEqualTo(ptLessons.getLast().getLessonStart());
		assertThat(ptLessonsResult.getLast().getLessonEnd()).isEqualTo(ptLessons.getLast().getLessonEnd());
		assertThat(ptLessonsResult.getLast().getSession()).isEqualTo(6);
		assertThat(ptLessonsResult.getLast().getMemo()).isEqualTo(ptLessons.getLast().getMemo());
		assertThat(ptLessonsResult.getLast().getPtTrainerTrainee()).isEqualTo(ptTrainerTrainee);
	}

	@Test
	@DisplayName("통합 테스트 - 예약된 PT 수업들 사이에 PT 수업 추가 성공")
	void add_pt_lesson_success3() throws Exception {
		// given
		Member trainerMember = MemberFixture.getTrainerMember1();
		Member traineeMember = MemberFixture.getTraineeMember1();

		trainerMember = memberRepository.save(trainerMember);
		traineeMember = memberRepository.save(traineeMember);

		CustomUserDetails trainerUserDetails = new CustomUserDetails(trainerMember.getId(),
			trainerMember.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(trainerUserDetails, null,
			authoritiesMapper.mapAuthorities(trainerUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		Trainer trainer = TrainerFixture.getTrainer2(trainerMember);
		Trainee trainee = TraineeFixture.getTrainee2(traineeMember);

		trainer = trainerRepository.save(trainer);
		trainee = traineeRepository.save(trainee);

		PtTrainerTrainee ptTrainerTrainee = PtTrainerTraineeFixture.getPtTrainerTrainee1(trainer, trainee);

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

		LocalDateTime startDate1 = LocalDateTime.parse("2025-02-01T11:30");
		LocalDateTime endDate1 = LocalDateTime.parse("2025-02-01T13:00");

		LocalDateTime startDate2 = LocalDateTime.parse("2025-02-07T11:30");
		LocalDateTime endDate2 = LocalDateTime.parse("2025-02-07T13:00");

		List<PtLesson> ptLessons = List.of(PtLesson.builder()
				.ptTrainerTrainee(ptTrainerTrainee)
				.session(4)
				.lessonStart(startDate1)
				.lessonEnd(endDate1)
				.build(),
			PtLesson.builder()
				.ptTrainerTrainee(ptTrainerTrainee)
				.session(5)
				.lessonStart(startDate2)
				.lessonEnd(endDate2)
				.build());

		ptLessonRepository.saveAll(ptLessons);

		LocalDateTime start = LocalDateTime.of(2025, 2, 5, 10, 0);
		LocalDateTime end = LocalDateTime.of(2025, 2, 5, 11, 0);
		String memo = "THIS IS MEMO";

		CreatePtLessonRequest request = new CreatePtLessonRequest(start, end, memo, trainee.getId());

		// when & then
		mockMvc.perform(post("/trainers/lessons")
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andDo(print());

		List<PtLesson> ptLessonsResult = ptLessonRepository.findAll().stream()
			.sorted(comparing(PtLesson::getLessonStart))
			.toList();

		assertThat(ptLessonsResult).hasSize(3);
		assertThat(ptLessonsResult.getFirst().getLessonStart()).isEqualTo(ptLessons.getFirst().getLessonStart());
		assertThat(ptLessonsResult.getFirst().getLessonEnd()).isEqualTo(ptLessons.getFirst().getLessonEnd());
		assertThat(ptLessonsResult.getFirst().getSession()).isEqualTo(4);
		assertThat(ptLessonsResult.getFirst().getMemo()).isEqualTo(ptLessons.getFirst().getMemo());
		assertThat(ptLessonsResult.getFirst().getPtTrainerTrainee()).isEqualTo(ptTrainerTrainee);
		assertThat(ptLessonsResult.get(1).getLessonStart()).isEqualTo(start);
		assertThat(ptLessonsResult.get(1).getLessonEnd()).isEqualTo(end);
		assertThat(ptLessonsResult.get(1).getSession()).isEqualTo(5);
		assertThat(ptLessonsResult.get(1).getMemo()).isEqualTo(memo);
		assertThat(ptLessonsResult.get(1).getPtTrainerTrainee()).isEqualTo(ptTrainerTrainee);
		assertThat(ptLessonsResult.getLast().getLessonStart()).isEqualTo(ptLessons.getLast().getLessonStart());
		assertThat(ptLessonsResult.getLast().getLessonEnd()).isEqualTo(ptLessons.getLast().getLessonEnd());
		assertThat(ptLessonsResult.getLast().getSession()).isEqualTo(6);
		assertThat(ptLessonsResult.getLast().getMemo()).isEqualTo(ptLessons.getLast().getMemo());
		assertThat(ptLessonsResult.getLast().getPtTrainerTrainee()).isEqualTo(ptTrainerTrainee);
	}

	@Test
	@DisplayName("통합 테스트 - 예약된 PT 수업들 보다 가장 늦은 날짜 PT 수업 추가 성공")
	void add_pt_lesson_success4() throws Exception {
		// given
		Member trainerMember = MemberFixture.getTrainerMember1();
		Member traineeMember = MemberFixture.getTraineeMember1();

		trainerMember = memberRepository.save(trainerMember);
		traineeMember = memberRepository.save(traineeMember);

		CustomUserDetails trainerUserDetails = new CustomUserDetails(trainerMember.getId(),
			trainerMember.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(trainerUserDetails, null,
			authoritiesMapper.mapAuthorities(trainerUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		Trainer trainer = TrainerFixture.getTrainer2(trainerMember);
		Trainee trainee = TraineeFixture.getTrainee2(traineeMember);

		trainer = trainerRepository.save(trainer);
		trainee = traineeRepository.save(trainee);

		PtTrainerTrainee ptTrainerTrainee = PtTrainerTraineeFixture.getPtTrainerTrainee1(trainer, trainee);

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

		LocalDateTime startDate1 = LocalDateTime.parse("2025-02-01T11:30");
		LocalDateTime endDate1 = LocalDateTime.parse("2025-02-01T13:00");

		LocalDateTime startDate2 = LocalDateTime.parse("2025-02-07T11:30");
		LocalDateTime endDate2 = LocalDateTime.parse("2025-02-07T13:00");

		List<PtLesson> ptLessons = List.of(PtLesson.builder()
				.ptTrainerTrainee(ptTrainerTrainee)
				.session(4)
				.lessonStart(startDate1)
				.lessonEnd(endDate1)
				.build(),
			PtLesson.builder()
				.ptTrainerTrainee(ptTrainerTrainee)
				.session(5)
				.lessonStart(startDate2)
				.lessonEnd(endDate2)
				.build());

		ptLessonRepository.saveAll(ptLessons);

		LocalDateTime start = LocalDateTime.of(2025, 3, 5, 10, 0);
		LocalDateTime end = LocalDateTime.of(2025, 3, 5, 11, 0);
		String memo = "THIS IS MEMO";

		CreatePtLessonRequest request = new CreatePtLessonRequest(start, end, memo, trainee.getId());

		// when & then
		mockMvc.perform(post("/trainers/lessons")
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andDo(print());

		List<PtLesson> ptLessonsResult = ptLessonRepository.findAll().stream()
			.sorted(comparing(PtLesson::getLessonStart))
			.toList();

		assertThat(ptLessonsResult).hasSize(3);
		assertThat(ptLessonsResult.getFirst().getLessonStart()).isEqualTo(ptLessons.getFirst().getLessonStart());
		assertThat(ptLessonsResult.getFirst().getLessonEnd()).isEqualTo(ptLessons.getFirst().getLessonEnd());
		assertThat(ptLessonsResult.getFirst().getSession()).isEqualTo(4);
		assertThat(ptLessonsResult.getFirst().getMemo()).isEqualTo(ptLessons.getFirst().getMemo());
		assertThat(ptLessonsResult.getFirst().getPtTrainerTrainee()).isEqualTo(ptTrainerTrainee);
		assertThat(ptLessonsResult.get(1).getLessonStart()).isEqualTo(ptLessons.getLast().getLessonStart());
		assertThat(ptLessonsResult.get(1).getLessonEnd()).isEqualTo(ptLessons.getLast().getLessonEnd());
		assertThat(ptLessonsResult.get(1).getSession()).isEqualTo(5);
		assertThat(ptLessonsResult.get(1).getMemo()).isEqualTo(ptLessons.getLast().getMemo());
		assertThat(ptLessonsResult.get(1).getPtTrainerTrainee()).isEqualTo(ptTrainerTrainee);
		assertThat(ptLessonsResult.getLast().getLessonStart()).isEqualTo(start);
		assertThat(ptLessonsResult.getLast().getLessonEnd()).isEqualTo(end);
		assertThat(ptLessonsResult.getLast().getSession()).isEqualTo(6);
		assertThat(ptLessonsResult.getLast().getMemo()).isEqualTo(memo);
		assertThat(ptLessonsResult.getLast().getPtTrainerTrainee()).isEqualTo(ptTrainerTrainee);
	}

	@Test
	@DisplayName("통합 테스트 - 중복된 시간이 겹치는 경우 PT 수업 추가 실패")
	void add_pt_lesson_fail1() throws Exception {
		// given
		Member trainerMember = MemberFixture.getTrainerMember1();
		Member traineeMember = MemberFixture.getTraineeMember1();

		trainerMember = memberRepository.save(trainerMember);
		traineeMember = memberRepository.save(traineeMember);

		CustomUserDetails trainerUserDetails = new CustomUserDetails(trainerMember.getId(),
			trainerMember.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(trainerUserDetails, null,
			authoritiesMapper.mapAuthorities(trainerUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		Trainer trainer = TrainerFixture.getTrainer2(trainerMember);
		Trainee trainee = TraineeFixture.getTrainee1(traineeMember);

		trainer = trainerRepository.save(trainer);
		trainee = traineeRepository.save(trainee);

		PtTrainerTrainee ptTrainerTrainee = PtTrainerTraineeFixture.getPtTrainerTrainee1(trainer, trainee);

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

		LocalDateTime createdStart = LocalDateTime.of(2025, 1, 1, 10, 0);
		LocalDateTime createdEnd = LocalDateTime.of(2025, 1, 1, 11, 0);

		LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 30);
		LocalDateTime end = LocalDateTime.of(2025, 1, 1, 11, 30);

		ptLessonRepository.save(PtLesson.builder()
			.ptTrainerTrainee(ptTrainerTrainee)
			.session(1)
			.lessonStart(createdStart)
			.lessonEnd(createdEnd)
			.build());

		CreatePtLessonRequest request = new CreatePtLessonRequest(start, end, null, trainee.getId());

		mockMvc.perform(post("/trainers/lessons")
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().is4xxClientError());
	}

	@Test
	@DisplayName("통합 테스트 - 이미 수업이 전부 예약되어 있을 때 PT 수업 추가 실패")
	void add_pt_lesson_fail2() throws Exception {
		// given
		Member trainerMember = MemberFixture.getTrainerMember1();
		Member traineeMember = MemberFixture.getTraineeMember1();

		trainerMember = memberRepository.save(trainerMember);
		traineeMember = memberRepository.save(traineeMember);

		CustomUserDetails trainerUserDetails = new CustomUserDetails(trainerMember.getId(),
			trainerMember.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(trainerUserDetails, null,
			authoritiesMapper.mapAuthorities(trainerUserDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		Trainer trainer = TrainerFixture.getTrainer2(trainerMember);
		Trainee trainee = TraineeFixture.getTrainee1(traineeMember);

		trainer = trainerRepository.save(trainer);
		trainee = traineeRepository.save(trainee);

		PtTrainerTrainee ptTrainerTrainee = PtTrainerTraineeFixture.getPtTrainerTrainee3(trainer, trainee);

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

		LocalDateTime startDate1 = LocalDateTime.parse("2025-02-01T11:30");
		LocalDateTime endDate1 = LocalDateTime.parse("2025-02-01T13:00");

		LocalDateTime startDate2 = LocalDateTime.parse("2025-02-07T11:30");
		LocalDateTime endDate2 = LocalDateTime.parse("2025-02-07T13:00");

		List<PtLesson> ptLessons = List.of(PtLesson.builder()
				.ptTrainerTrainee(ptTrainerTrainee)
				.session(9)
				.lessonStart(startDate1)
				.lessonEnd(endDate1)
				.build(),
			PtLesson.builder()
				.ptTrainerTrainee(ptTrainerTrainee)
				.session(10)
				.lessonStart(startDate2)
				.lessonEnd(endDate2)
				.build());

		ptLessonRepository.saveAll(ptLessons);

		LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 30);
		LocalDateTime end = LocalDateTime.of(2025, 1, 1, 11, 30);
		String memo = "THIS IS MEMO";

		CreatePtLessonRequest request = new CreatePtLessonRequest(start, end, memo, trainee.getId());

		mockMvc.perform(post("/trainers/lessons")
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().is5xxServerError());
	}

	@Test
	@DisplayName("통합 테스트 - PT 수업 완료 성공")
	void complete_pt_lesson_success() throws Exception {
		// given
		Member trainerMember = MemberFixture.getTrainerMember1();
		Member traineeMember = MemberFixture.getTraineeMember1();

		trainerMember = memberRepository.save(trainerMember);
		traineeMember = memberRepository.save(traineeMember);

		CustomUserDetails trainerUserDetails = new CustomUserDetails(trainerMember.getId(),
			trainerMember.getId().toString(),
			authoritiesMapper.mapAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		Authentication authentication = new UsernamePasswordAuthenticationToken(trainerUserDetails, null,
			authoritiesMapper.mapAuthorities(trainerUserDetails.getAuthorities()));

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

		PtTrainerTrainee ptTrainerTrainee = PtTrainerTraineeFixture.getPtTrainerTrainee1(trainer, trainee);

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

		PtLesson ptLesson = PtLesson.builder()
			.ptTrainerTrainee(ptTrainerTrainee)
			.session(1)
			.lessonStart(LocalDateTime.of(2025, 1, 1, 10, 0))
			.lessonEnd(LocalDateTime.of(2025, 1, 1, 11, 0))
			.memo("THIS IS MEMO")
			.build();

		ptLesson = ptLessonRepository.save(ptLesson);

		// when & then
		assertThat(ptLesson.getIsCompleted()).isFalse();
		mockMvc.perform(put("/trainers/lessons/{ptLessonId}/complete", ptLesson.getId()))
			.andExpect(status().isOk());
		//noinspection OptionalGetWithoutIsPresent
		assertThat(ptLessonRepository.findById(ptLesson.getId()).get().getIsCompleted()).isTrue();
	}
}
