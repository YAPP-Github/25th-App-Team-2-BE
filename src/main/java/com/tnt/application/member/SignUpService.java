package com.tnt.application.member;

import static com.tnt.common.constant.ProfileConstant.TRAINEE_DEFAULT_IMAGE;
import static com.tnt.common.constant.ProfileConstant.TRAINER_DEFAULT_IMAGE;
import static com.tnt.domain.member.MemberType.TRAINEE;
import static com.tnt.domain.member.MemberType.TRAINER;
import static io.hypersistence.tsid.TSID.Factory.getTsid;
import static io.micrometer.common.util.StringUtils.isNotBlank;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tnt.application.trainee.PtGoalService;
import com.tnt.application.trainee.TraineeService;
import com.tnt.application.trainer.TrainerService;
import com.tnt.domain.member.Member;
import com.tnt.domain.member.MemberType;
import com.tnt.domain.trainee.PtGoal;
import com.tnt.domain.trainee.Trainee;
import com.tnt.domain.trainer.Trainer;
import com.tnt.dto.member.request.SignUpRequest;
import com.tnt.dto.member.response.SignUpResponse;
import com.tnt.gateway.service.SessionService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SignUpService {

	private final SessionService sessionService;
	private final MemberService memberService;
	private final TrainerService trainerService;
	private final TraineeService traineeService;
	private final PtGoalService ptGoalService;

	@Transactional
	public Long signUp(SignUpRequest request) {
		memberService.validateMemberNotExists(request.socialId(), request.socialType());

		if (TRAINER.equals(request.memberType())) {
			return createTrainer(request);
		}

		return createTrainee(request);
	}

	@Transactional
	public SignUpResponse finishSignUpWithImage(String profileImageUrl, Long memberId, MemberType memberType) {
		Member member = memberService.getMemberWithMemberId(memberId);

		member.updateProfileImageUrl(profileImageUrl);

		String sessionId = String.valueOf(getTsid());

		sessionService.createSession(sessionId, String.valueOf(member.getId()));

		return new SignUpResponse(memberType, sessionId, member.getName(), member.getProfileImageUrl());
	}

	private Long createTrainer(SignUpRequest request) {
		Member member = createMember(request, TRAINER_DEFAULT_IMAGE, TRAINER);
		Trainer trainer = Trainer.builder()
			.member(member)
			.build();

		trainerService.saveTrainer(trainer);

		return member.getId();
	}

	private Long createTrainee(SignUpRequest request) {
		Member member = createMember(request, TRAINEE_DEFAULT_IMAGE, TRAINEE);
		Trainee trainee = Trainee.builder()
			.member(member)
			.height(request.height())
			.weight(request.weight())
			.cautionNote(isNotBlank(request.cautionNote()) ? request.cautionNote() : "")
			.build();

		trainee = traineeService.saveTrainee(trainee);

		createPtGoals(trainee, request.goalContents());

		return member.getId();
	}

	private Member createMember(SignUpRequest request, String defaultImageUrl, MemberType memberType) {
		Member member = Member.builder()
			.socialId(request.socialId())
			.fcmToken(request.fcmToken())
			.email(request.socialEmail())
			.name(request.name())
			.profileImageUrl(defaultImageUrl)
			.birthday(request.birthday())
			.serviceAgreement(request.serviceAgreement())
			.collectionAgreement(request.collectionAgreement())
			.advertisementAgreement(request.advertisementAgreement())
			.socialType(request.socialType())
			.memberType(memberType)
			.build();

		return memberService.saveMember(member);
	}

	private void createPtGoals(Trainee trainee, List<String> goalContents) {
		List<PtGoal> ptGoals = goalContents.stream()
			.map(content -> PtGoal.builder()
				.traineeId(trainee.getId())
				.content(content)
				.build())
			.toList();

		ptGoalService.saveAllPtGoals(ptGoals);
	}
}
