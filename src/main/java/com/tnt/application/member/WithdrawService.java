package com.tnt.application.member;

import static com.tnt.common.error.model.ErrorMessage.UNSUPPORTED_MEMBER_TYPE;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tnt.application.pt.PtService;
import com.tnt.application.trainee.PtGoalService;
import com.tnt.application.trainee.TraineeService;
import com.tnt.application.trainer.TrainerService;
import com.tnt.domain.member.Member;
import com.tnt.domain.pt.PtTrainerTrainee;
import com.tnt.domain.trainee.PtGoal;
import com.tnt.domain.trainee.Trainee;
import com.tnt.domain.trainer.Trainer;
import com.tnt.dto.member.request.WithdrawRequest;
import com.tnt.gateway.service.OAuthService;
import com.tnt.gateway.service.SessionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WithdrawService {

	private final SessionService sessionService;
	private final OAuthService oAuthService;
	private final MemberService memberService;
	private final TrainerService trainerService;
	private final TraineeService traineeService;
	private final PtGoalService ptGoalService;
	private final PtService ptService;

	@Transactional
	public void withdraw(String memberId, WithdrawRequest request) {
		Member member = memberService.getMemberWithMemberId(memberId);

		softDeleteWithMemberData(member);

		oAuthService.revoke(member.getSocialId(), member.getSocialType(), request);

		sessionService.removeSession(memberId);
	}

	private void softDeleteWithMemberData(Member member) {
		switch (member.getMemberType()) {
			case TRAINER -> {
				Trainer trainer = trainerService.getTrainerWithMemberId(String.valueOf(member.getId()));
				PtTrainerTrainee ptTrainerTrainee = ptService.getPtTrainerTraineeWithTrainerId(trainer.getId());

				ptService.softDeletePtTrainerTrainee(ptTrainerTrainee);
				trainerService.softDeleteTrainer(trainer);
			}
			case TRAINEE -> {
				Trainee trainee = traineeService.getTraineeWithMemberId(String.valueOf(member.getId()));
				List<PtGoal> ptGoals = ptGoalService.getAllPtGoalsWithTraineeId(trainee.getId());
				PtTrainerTrainee ptTrainerTrainee = ptService.getPtTrainerTraineeWithTraineeId(trainee.getId());

				ptGoalService.softDeleteAllPtGoals(ptGoals);
				ptService.softDeletePtTrainerTrainee(ptTrainerTrainee);
				traineeService.softDeleteTrainee(trainee);
			}
			default -> throw new IllegalArgumentException(UNSUPPORTED_MEMBER_TYPE.getMessage());
		}

		memberService.softDeleteMember(member);
	}
}
