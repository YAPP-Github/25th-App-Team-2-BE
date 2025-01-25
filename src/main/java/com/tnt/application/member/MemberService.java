package com.tnt.application.member;

import static com.tnt.domain.constant.Constant.*;
import static com.tnt.global.error.model.ErrorMessage.*;
import static io.hypersistence.tsid.TSID.Factory.*;
import static io.micrometer.common.util.StringUtils.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tnt.application.auth.SessionService;
import com.tnt.domain.member.Member;
import com.tnt.domain.member.SocialType;
import com.tnt.domain.trainee.PtGoal;
import com.tnt.domain.trainee.Trainee;
import com.tnt.domain.trainer.Trainer;
import com.tnt.dto.member.request.SignUpRequest;
import com.tnt.dto.member.response.SignUpResponse;
import com.tnt.global.error.exception.ConflictException;
import com.tnt.global.error.exception.NotFoundException;
import com.tnt.infrastructure.mysql.repository.member.MemberRepository;
import com.tnt.infrastructure.mysql.repository.trainee.PtGoalRepository;
import com.tnt.infrastructure.mysql.repository.trainee.TraineeRepository;
import com.tnt.infrastructure.mysql.repository.trainer.TrainerRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final TrainerRepository trainerRepository;
	private final TraineeRepository traineeRepository;
	private final PtGoalRepository ptGoalRepository;
	private final SessionService sessionService;

	@Transactional
	public Long signUp(SignUpRequest request) {
		validateMemberNotExists(request.socialId(), request.socialType());

		return switch (request.memberType()) {
			case TRAINER -> createTrainer(request);
			case TRAINEE -> createTrainee(request);
			default -> throw new IllegalArgumentException(UNSUPPORTED_MEMBER_TYPE.getMessage());
		};
	}

	@Transactional
	public SignUpResponse finishSignUpWithImage(String profileImageUrl, Long memberId, String memberType) {
		Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId)
			.orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));

		member.updateProfileImageUrl(profileImageUrl);

		String sessionId = String.valueOf(getTsid());

		sessionService.createSession(sessionId, String.valueOf(member.getId()));

		return new SignUpResponse(memberType, sessionId, member.getName(), member.getProfileImageUrl());
	}

	private void validateMemberNotExists(String socialId, String socialType) {
		memberRepository.findBySocialIdAndSocialTypeAndDeletedAtIsNull(socialId, SocialType.valueOf(socialType))
			.ifPresent(member -> {
				throw new ConflictException(MEMBER_CONFLICT);
			});
	}

	private Member createMember(SignUpRequest request, String defaultImageUrl) {
		Member member = Member.builder()
			.socialId(request.socialId())
			.fcmToken(request.fcmToken())
			.email(request.socialEmail())
			.name(request.name())
			.profileImageUrl(defaultImageUrl)
			.serviceAgreement(request.serviceAgreement())
			.collectionAgreement(request.collectionAgreement())
			.advertisementAgreement(request.advertisementAgreement())
			.pushAgreement(request.pushAgreement())
			.socialType(SocialType.valueOf(request.socialType()))
			.build();

		return memberRepository.save(member);
	}

	private Long createTrainer(SignUpRequest request) {
		Member member = createMember(request, TRAINER_DEFAULT_IMAGE);
		Trainer trainer = Trainer.builder()
			.member(member)
			.build();

		trainerRepository.save(trainer);

		return member.getId();
	}

	private Long createTrainee(SignUpRequest request) {
		Member member = createMember(request, TRAINEE_DEFAULT_IMAGE);
		Trainee trainee = Trainee.builder()
			.member(member)
			.height(request.height())
			.weight(request.weight())
			.cautionNote(isNotBlank(request.cautionNote()) ? request.cautionNote() : "")
			.build();

		trainee = traineeRepository.save(trainee);

		createPtGoals(trainee, request.goalContents());

		return member.getId();
	}

	private void createPtGoals(Trainee trainee, List<String> goalContents) {
		List<PtGoal> ptGoals = goalContents.stream()
			.map(content -> PtGoal.builder()
				.traineeId(trainee.getId())
				.content(content)
				.build())
			.toList();

		ptGoalRepository.saveAll(ptGoals);
	}
}
