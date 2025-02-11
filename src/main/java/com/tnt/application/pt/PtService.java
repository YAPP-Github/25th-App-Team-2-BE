package com.tnt.application.pt;

import static com.tnt.common.error.model.ErrorMessage.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tnt.application.trainee.PtGoalService;
import com.tnt.application.trainee.TraineeService;
import com.tnt.application.trainer.TrainerService;
import com.tnt.common.error.exception.ConflictException;
import com.tnt.common.error.exception.NotFoundException;
import com.tnt.domain.member.Member;
import com.tnt.domain.pt.PtLesson;
import com.tnt.domain.pt.PtTrainerTrainee;
import com.tnt.domain.trainee.PtGoal;
import com.tnt.domain.trainee.Trainee;
import com.tnt.domain.trainer.Trainer;
import com.tnt.dto.trainer.ConnectWithTrainerDto;
import com.tnt.dto.trainer.request.ConnectWithTrainerRequest;
import com.tnt.dto.trainer.request.CreatePtLessonRequest;
import com.tnt.dto.trainer.response.ConnectWithTraineeResponse;
import com.tnt.dto.trainer.response.ConnectWithTraineeResponse.ConnectTraineeInfo;
import com.tnt.dto.trainer.response.ConnectWithTraineeResponse.ConnectTrainerInfo;
import com.tnt.dto.trainer.response.GetActiveTraineesResponse;
import com.tnt.dto.trainer.response.GetActiveTraineesResponse.TraineeInfo;
import com.tnt.dto.trainer.response.GetCalendarPtLessonCountResponse;
import com.tnt.dto.trainer.response.GetCalendarPtLessonCountResponse.CalendarPtLessonCount;
import com.tnt.dto.trainer.response.GetPtLessonsOnDateResponse;
import com.tnt.dto.trainer.response.GetPtLessonsOnDateResponse.Lesson;
import com.tnt.infrastructure.mysql.repository.pt.PtLessonRepository;
import com.tnt.infrastructure.mysql.repository.pt.PtLessonSearchRepository;
import com.tnt.infrastructure.mysql.repository.pt.PtTrainerTraineeRepository;
import com.tnt.infrastructure.mysql.repository.pt.PtTrainerTraineeSearchRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PtService {

	private final TraineeService traineeService;
	private final TrainerService trainerService;
	private final PtGoalService ptGoalService;
	private final PtTrainerTraineeRepository ptTrainerTraineeRepository;
	private final PtLessonRepository ptLessonRepository;
	private final PtLessonSearchRepository ptLessonSearchRepository;
	private final PtTrainerTraineeSearchRepository ptTrainerTraineeSearchRepository;

	@Transactional
	public ConnectWithTrainerDto connectWithTrainer(Long memberId, ConnectWithTrainerRequest request) {
		Trainer trainer = trainerService.getTrainerWithInvitationCode(request.invitationCode());
		Trainee trainee = traineeService.getTraineeWithMemberId(memberId);

		validateNotAlreadyConnected(trainer.getId(), trainee.getId());

		PtTrainerTrainee ptTrainerTrainee = PtTrainerTrainee.builder()
			.trainer(trainer)
			.trainee(trainee)
			.startedAt(request.startDate())
			.finishedPtCount(request.finishedPtCount())
			.totalPtCount(request.totalPtCount())
			.build();

		ptTrainerTraineeRepository.save(ptTrainerTrainee);

		Member trainerMember = trainer.getMember(); // fetch join 으로 가져온 member
		Member traineeMember = trainee.getMember(); // fetch join 으로 가져온 member

		return new ConnectWithTrainerDto(trainerMember.getFcmToken(), trainerMember.getName(), traineeMember.getName(),
			trainerMember.getProfileImageUrl(), traineeMember.getProfileImageUrl(), trainer.getId(), trainee.getId());
	}

	@Transactional(readOnly = true)
	public ConnectWithTraineeResponse getFirstTrainerTraineeConnect(Long memberId, Long trainerId,
		Long traineeId) {
		validateIfNotConnected(trainerId, traineeId);

		Trainer trainer = trainerService.getTrainerWithMemberId(memberId);
		Trainee trainee = traineeService.getTraineeWithId(traineeId);

		Member trainerMember = trainer.getMember(); // fetch join 으로 가져온 member
		Member traineeMember = trainee.getMember(); // fetch join 으로 가져온 member

		List<PtGoal> ptGoals = ptGoalService.getAllPtGoalsWithTraineeId(traineeId);
		String ptGoal = ptGoals.stream().map(PtGoal::getContent).collect(Collectors.joining(", "));

		return new ConnectWithTraineeResponse(
			new ConnectTrainerInfo(trainerMember.getName(), trainerMember.getProfileImageUrl()),
			new ConnectTraineeInfo(traineeMember.getName(), traineeMember.getProfileImageUrl(),
				traineeMember.getAge(), trainee.getHeight(), trainee.getWeight(), ptGoal, trainee.getCautionNote())
		);
	}

	@Transactional(readOnly = true)
	public GetPtLessonsOnDateResponse getPtLessonsOnDate(Long memberId, LocalDate date) {
		Trainer trainer = trainerService.getTrainerWithMemberId(memberId);

		List<PtLesson> ptLessons = ptLessonSearchRepository.findAllByTrainerIdAndDate(trainer.getId(), date);

		List<Lesson> lessons = ptLessons.stream().map(ptLesson -> {
			PtTrainerTrainee ptTrainerTrainee = ptLesson.getPtTrainerTrainee();
			Trainee trainee = ptTrainerTrainee.getTrainee();

			return new Lesson(String.valueOf(ptLesson.getId()),
				String.valueOf(trainee.getId()), trainee.getMember().getName(),
				trainee.getMember().getProfileImageUrl(), ptTrainerTrainee.getCurrentPtSession(),
				ptLesson.getLessonStart(), ptLesson.getLessonEnd(), ptLesson.getIsCompleted());
		}).toList();

		return new GetPtLessonsOnDateResponse(ptLessons.size(), date, lessons);
	}

	@Transactional(readOnly = true)
	public GetCalendarPtLessonCountResponse getCalendarPtLessonCount(Long memberId, Integer year, Integer month) {
		Trainer trainer = trainerService.getTrainerWithMemberId(memberId);

		List<PtLesson> ptLessons = ptLessonSearchRepository.findAllByTraineeIdForCalendar(trainer.getId(), year, month);

		List<CalendarPtLessonCount> counts = ptLessons.stream()
			.collect(Collectors.groupingBy(
				lesson -> lesson.getLessonStart().toLocalDate(),
				LinkedHashMap::new,
				Collectors.counting()
			))
			.entrySet().stream()
			.map(entry -> new CalendarPtLessonCount(entry.getKey(), entry.getValue().intValue()))
			.toList();

		return new GetCalendarPtLessonCountResponse(counts);
	}

	@Transactional(readOnly = true)
	public GetActiveTraineesResponse getActiveTrainees(Long memberId) {
		Trainer trainer = trainerService.getTrainerWithMemberId(memberId);

		List<Trainee> trainees = ptTrainerTraineeSearchRepository.findAllTrainees(trainer.getId());

		List<TraineeInfo> traineeInfo = trainees.stream().map(trainee -> {
			PtTrainerTrainee ptTrainerTrainee = ptTrainerTraineeRepository.findByTraineeIdAndDeletedAtIsNull(
					trainee.getId())
				.orElseThrow(() -> new NotFoundException(TRAINEE_NOT_FOUND));

			List<String> ptGoals = ptGoalService.getAllPtGoalsWithTraineeId(trainee.getId())
				.stream()
				.map(PtGoal::getContent)
				.toList();

			return new TraineeInfo(trainee.getId(), trainee.getMember().getName(),
				trainee.getMember().getProfileImageUrl(), ptTrainerTrainee.getFinishedPtCount(),
				ptTrainerTrainee.getTotalPtCount(), trainee.getCautionNote(), ptGoals);
		}).toList();

		return new GetActiveTraineesResponse(trainees.size(), traineeInfo);
	}

	@Transactional
	public void addPtLesson(Long memberId, CreatePtLessonRequest request) {
		trainerService.validateTrainerRegistration(memberId);

		PtTrainerTrainee ptTrainerTrainee = getPtTrainerTraineeWithTraineeId(request.traineeId());

		// 트레이너의 기존 pt 수업중에 중복되는 시간대가 있는지 확인
		validateLessonTime(ptTrainerTrainee, request.start(), request.end());

		PtLesson ptLesson = PtLesson.builder()
			.ptTrainerTrainee(ptTrainerTrainee)
			.lessonStart(request.start())
			.lessonEnd(request.end())
			.memo(request.memo())
			.build();

		ptLessonRepository.save(ptLesson);
	}

	@Transactional
	public void completePtLesson(Long memberId, Long ptLessonId) {
		trainerService.validateTrainerRegistration(memberId);

		PtLesson ptLesson = getPtLessonWithId(ptLessonId);
		ptLesson.completeLesson();
	}

	public boolean isPtTrainerTraineeExistWithTrainerId(Long trainerId) {
		return ptTrainerTraineeRepository.existsByTrainerIdAndDeletedAtIsNull(trainerId);
	}

	public boolean isPtTrainerTraineeExistWithTraineeId(Long traineeId) {
		return ptTrainerTraineeRepository.existsByTraineeIdAndDeletedAtIsNull(traineeId);
	}

	public List<PtTrainerTrainee> getAllPtTrainerTraineeWithTrainerId(Long trainerId) {
		return ptTrainerTraineeRepository.findAllByTrainerIdAndDeletedAtIsNull(trainerId);
	}

	public List<PtTrainerTrainee> getAllPtTrainerTraineeWithTrainerIdWithDeleted(Long trainerId) {
		return ptTrainerTraineeRepository.findAllByTrainerId(trainerId);
	}

	public PtTrainerTrainee getPtTrainerTraineeWithTrainerId(Long trainerId) {
		return ptTrainerTraineeRepository.findByTrainerIdAndDeletedAtIsNull(trainerId)
			.orElseThrow(() -> new NotFoundException(PT_TRAINER_TRAINEE_NOT_FOUND));
	}

	public PtTrainerTrainee getPtTrainerTraineeWithTraineeId(Long traineeId) {
		return ptTrainerTraineeRepository.findByTraineeIdAndDeletedAtIsNull(traineeId)
			.orElseThrow(() -> new NotFoundException(PT_TRAINER_TRAINEE_NOT_FOUND));
	}

	public List<PtLesson> getPtLessonWithPtTrainerTrainee(PtTrainerTrainee ptTrainerTrainee) {
		return ptLessonRepository.findAllByPtTrainerTraineeAndDeletedAtIsNull(ptTrainerTrainee);
	}

	public PtLesson getPtLessonWithId(Long ptLessonId) {
		return ptLessonRepository.findByIdAndDeletedAtIsNull(ptLessonId)
			.orElseThrow(() -> new NotFoundException(PT_LESSON_NOT_FOUND));
	}

	private void validateNotAlreadyConnected(Long trainerId, Long traineeId) {
		if (ptTrainerTraineeRepository.existsByTraineeIdAndDeletedAtIsNull(traineeId)) {
			throw new ConflictException(PT_TRAINEE_ALREADY_EXIST);
		}

		if (ptTrainerTraineeRepository.existsByTrainerIdAndTraineeIdAndDeletedAtIsNull(trainerId, traineeId)) {
			throw new ConflictException(PT_TRAINER_TRAINEE_ALREADY_EXIST);
		}
	}

	private void validateIfNotConnected(Long trainerId, Long traineeId) {
		if (!ptTrainerTraineeRepository.existsByTrainerIdAndTraineeIdAndDeletedAtIsNull(trainerId, traineeId)) {
			throw new NotFoundException(PT_TRAINER_TRAINEE_NOT_FOUND);
		}
	}

	private void validateLessonTime(PtTrainerTrainee ptTrainerTrainee, LocalDateTime start, LocalDateTime end) {
		if (ptLessonSearchRepository.existsByStartAndEnd(ptTrainerTrainee, start, end)) {
			throw new ConflictException(PT_LESSON_DUPLICATE_TIME);
		}
	}
}
