package com.tnt.application.pt;

import static com.tnt.common.error.model.ErrorMessage.PT_TRAINEE_ALREADY_EXIST;
import static com.tnt.common.error.model.ErrorMessage.PT_TRAINER_TRAINEE_ALREADY_EXIST;
import static com.tnt.common.error.model.ErrorMessage.PT_TRAINER_TRAINEE_NOT_FOUND;
import static java.util.Objects.isNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tnt.application.trainee.PtGoalService;
import com.tnt.application.trainee.TraineeService;
import com.tnt.application.trainer.TrainerService;
import com.tnt.common.error.exception.ConflictException;
import com.tnt.common.error.exception.NotFoundException;
import com.tnt.common.error.model.ErrorMessage;
import com.tnt.domain.member.Member;
import com.tnt.domain.pt.PtLesson;
import com.tnt.domain.pt.PtTrainerTrainee;
import com.tnt.domain.trainee.PtGoal;
import com.tnt.domain.trainee.Trainee;
import com.tnt.domain.trainer.Trainer;
import com.tnt.dto.trainer.ConnectWithTrainerDto;
import com.tnt.dto.trainer.request.ConnectWithTrainerRequest;
import com.tnt.dto.trainer.response.ConnectWithTraineeResponse;
import com.tnt.dto.trainer.response.GetPtLessonsOnDateResponse;
import com.tnt.dto.trainer.response.GetPtLessonsOnDateResponse.Lesson;
import com.tnt.infrastructure.mysql.repository.pt.PtGoalRepository;
import com.tnt.infrastructure.mysql.repository.pt.PtLessonSearchRepository;
import com.tnt.infrastructure.mysql.repository.pt.PtTrainerTraineeRepository;
import com.tnt.infrastructure.mysql.repository.trainee.PtGoalRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PtService {

	private final TraineeService traineeService;
	private final TrainerService trainerService;
	private final PtGoalService ptGoalService;
	private final PtTrainerTraineeRepository ptTrainerTraineeRepository;
	private final PtGoalRepository ptGoalRepository;
	private final PtLessonSearchRepository ptLessonSearchRepository;

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

	public ConnectWithTraineeResponse getFirstTrainerTraineeConnect(Long memberId, Long trainerId,
		Long traineeId) {
		validateIfNotConnected(trainerId, traineeId);

		Trainer trainer = trainerService.getTrainerWithMemberId(memberId);
		Trainee trainee = traineeService.getTraineeWithId(traineeId);

		Member trainerMember = trainer.getMember(); // fetch join 으로 가져온 member
		Member traineeMember = trainee.getMember(); // fetch join 으로 가져온 member

		List<PtGoal> ptGoals = ptGoalService.getAllPtGoalsWithTraineeId(Long.valueOf(traineeId));
		String ptGoal = ptGoals.stream().map(PtGoal::getContent).collect(Collectors.joining(", "));

		return new ConnectWithTraineeResponse(trainerMember.getName(), traineeMember.getName(),
			trainerMember.getProfileImageUrl(), traineeMember.getProfileImageUrl(), traineeMember.getAge(),
			trainee.getHeight(), trainee.getWeight(), ptGoal, trainee.getCautionNote());
	}

	public GetPtLessonsOnDateResponse getPtLessonsOnDate(Long memberId, LocalDate date) {
		Trainer trainer = trainerService.getTrainerWithMemberId(memberId);

		List<PtLesson> ptLessons = ptLessonSearchRepository.findAllByTrainerIdAndDate(trainer.getId(), date);

		List<Lesson> lessons = ptLessons.stream().map(ptLesson -> {
			PtTrainerTrainee ptTrainerTrainee = ptLesson.getPtTrainerTrainee();
			Trainee trainee = ptTrainerTrainee.getTrainee();

			return new Lesson(String.valueOf(ptLesson.getId()),
				String.valueOf(trainee.getId()), trainee.getMember().getName(), ptTrainerTrainee.getCurrentSession(),
				ptLesson.getLessonStart(), ptLesson.getLessonEnd(), ptLesson.getIsCompleted());
		}).toList();

		return new GetPtLessonsOnDateResponse(ptLessons.size(), date, lessons);
	}

	public Optional<PtTrainerTrainee> getPtTrainerTraineeWithTrainerId(Long trainerId) {
		return ptTrainerTraineeRepository.findByTrainerIdAndDeletedAtIsNull(trainerId);
	}

	public Optional<PtTrainerTrainee> getPtTrainerTraineeWithTraineeId(Long traineeId) {
		return ptTrainerTraineeRepository.findByTraineeIdAndDeletedAtIsNull(traineeId);
	}

	public void softDeletePtTrainerTrainee(PtTrainerTrainee ptTrainerTrainee) {
		LocalDateTime now = LocalDateTime.now();

		ptTrainerTrainee.updateDeletedAt(now);
	}

	private void validateNotAlreadyConnected(Long trainerId, Long traineeId) {
		if (ptTrainerTraineeRepository.existsByTraineeIdAndDeletedAtIsNull(traineeId)) {
			throw new ConflictException(PT_TRAINEE_ALREADY_EXIST);
		}

		if (ptTrainerTraineeRepository.existsByTrainerIdAndTraineeIdAndDeletedAtIsNull(trainerId, traineeId)) {
			throw new ConflictException(PT_TRAINER_TRAINEE_ALREADY_EXIST);
		}
	}

	private void validateIfNotConnected(String trainerId, String traineeId) {
		ptTrainerTraineeRepository.findByTrainerIdAndTraineeIdAndDeletedAtIsNull(Long.valueOf(trainerId),
				Long.valueOf(traineeId))
			.orElseThrow(() -> new NotFoundException(PT_TRAINER_TRAINEE_NOT_FOUND));
	}

	private void validateIfNotConnected(Long trainerId, Long traineeId) {
		if (!ptTrainerTraineeRepository.existsByTrainerIdAndTraineeIdAndDeletedAtIsNull(trainerId, traineeId)) {
			throw new NotFoundException(ErrorMessage.PT_TRAINER_TRAINEE_NOT_FOUND);
		}
	}
}
