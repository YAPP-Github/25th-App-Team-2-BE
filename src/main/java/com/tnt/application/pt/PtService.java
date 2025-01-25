package com.tnt.application.pt;

import static com.tnt.global.error.model.ErrorMessage.PT_TRAINEE_ALREADY_EXIST;
import static com.tnt.global.error.model.ErrorMessage.PT_TRAINER_TRAINEE_ALREADY_EXIST;
import static java.util.Objects.isNull;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tnt.application.trainee.TraineeService;
import com.tnt.application.trainer.TrainerService;
import com.tnt.domain.member.Member;
import com.tnt.domain.pt.PtTrainerTrainee;
import com.tnt.domain.trainee.PtGoal;
import com.tnt.domain.trainee.Trainee;
import com.tnt.domain.trainer.Trainer;
import com.tnt.dto.trainer.ConnectWithTrainerDto;
import com.tnt.dto.trainer.request.ConnectWithTrainerRequest;
import com.tnt.dto.trainer.response.ConnectWithTraineeResponse;
import com.tnt.global.error.exception.ConflictException;
import com.tnt.global.error.exception.NotFoundException;
import com.tnt.global.error.model.ErrorMessage;
import com.tnt.infrastructure.mysql.repository.pt.PtTrainerTraineeRepository;
import com.tnt.infrastructure.mysql.repository.trainee.PtGoalRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PtService {

	private final TraineeService traineeService;
	private final TrainerService trainerService;
	private final PtTrainerTraineeRepository ptTrainerTraineeRepository;
	private final PtGoalRepository ptGoalRepository;

	@Transactional
	public ConnectWithTrainerDto connectWithTrainer(String memberId, ConnectWithTrainerRequest request) {
		Trainer trainer = trainerService.getTrainerWithInvitationCode(request.invitationCode());
		Trainee trainee = traineeService.getTraineeWithMemberId(memberId);

		validateNotAlreadyConnected(trainer.getId(), trainee.getId());

		PtTrainerTrainee ptTrainerTrainee = PtTrainerTrainee.builder()
			.trainerId(trainer.getId())
			.traineeId(trainee.getId())
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

	public ConnectWithTraineeResponse getFirstTrainerTraineeConnect(String memberId, String trainerId,
		String traineeId) {
		validateIfNotConnected(trainerId, traineeId);

		Trainer trainer = trainerService.getTrainerWithMemberId(memberId);
		Trainee trainee = traineeService.getTraineeWithId(traineeId);

		Member trainerMember = trainer.getMember(); // fetch join 으로 가져온 member
		Member traineeMember = trainee.getMember(); // fetch join 으로 가져온 member

		String traineeAge = calculateCurrentAge(traineeMember.getBirthday());

		List<PtGoal> ptGoals = ptGoalRepository.findAllByTraineeId(Long.valueOf(traineeId));
		String ptGoal = getPtGoals(ptGoals);

		return new ConnectWithTraineeResponse(trainerMember.getName(), traineeMember.getName(),
			trainerMember.getProfileImageUrl(), traineeMember.getProfileImageUrl(), traineeAge, trainee.getHeight(),
			trainee.getWeight(), ptGoal, trainee.getCautionNote());
	}

	private void validateNotAlreadyConnected(Long trainerId, Long traineeId) {
		ptTrainerTraineeRepository.findByTraineeIdAndDeletedAtIsNull(traineeId)
			.ifPresent(pt -> { // 이미 다른 트레이너와 연결 중인지 확인
				throw new ConflictException(PT_TRAINEE_ALREADY_EXIST);
			});

		ptTrainerTraineeRepository.findByTrainerIdAndTraineeIdAndDeletedAtIsNull(trainerId, traineeId)
			.ifPresent(pt -> { // 이미 해당 트레이너와 연결 중인지 확인
				throw new ConflictException(PT_TRAINER_TRAINEE_ALREADY_EXIST);
			});
	}

	private void validateIfNotConnected(String trainerId, String traineeId) {
		ptTrainerTraineeRepository.findByTrainerIdAndTraineeIdAndDeletedAtIsNull(Long.valueOf(trainerId),
				Long.valueOf(traineeId))
			.orElseThrow(() -> new NotFoundException(ErrorMessage.PT_TRAINER_TRAINEE_NOT_FOUND));
	}

	private String calculateCurrentAge(LocalDate birthDay) {
		if (isNull(birthDay)) {
			return "비공개";
		}

		LocalDate currentDate = LocalDate.now();
		int age = currentDate.getYear() - birthDay.getYear();

		// 생일이 아직 지나지 않았으면 나이를 1 줄임
		if (currentDate.isBefore(birthDay.withYear(currentDate.getYear()))) {
			age--;
		}

		return String.valueOf(age);
	}

	private String getPtGoals(List<PtGoal> ptGoals) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < ptGoals.size(); i++) {
			sb.append(ptGoals.get(i).getContent());

			if (i != ptGoals.size() - 1) {
				sb.append(", ");
			}
		}

		return sb.toString();
	}
}
