package com.tnt.application.member;

import static com.tnt.common.constant.ProfileConstant.TRAINEE_DEFAULT_IMAGE;
import static com.tnt.common.constant.ProfileConstant.TRAINER_DEFAULT_IMAGE;
import static com.tnt.common.error.model.ErrorMessage.MEMBER_CONFLICT;
import static com.tnt.common.error.model.ErrorMessage.MEMBER_NOT_FOUND;
import static com.tnt.common.error.model.ErrorMessage.UNSUPPORTED_MEMBER_TYPE;
import static com.tnt.domain.member.MemberType.TRAINEE;
import static com.tnt.domain.member.MemberType.TRAINER;
import static io.hypersistence.tsid.TSID.Factory.getTsid;
import static io.micrometer.common.util.StringUtils.isNotBlank;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tnt.common.error.exception.ConflictException;
import com.tnt.common.error.exception.NotFoundException;
import com.tnt.domain.member.Member;
import com.tnt.domain.member.MemberType;
import com.tnt.domain.member.SocialType;
import com.tnt.domain.trainee.PtGoal;
import com.tnt.domain.trainee.Trainee;
import com.tnt.domain.trainer.Trainer;
import com.tnt.dto.member.request.SignUpRequest;
import com.tnt.dto.member.response.SignUpResponse;
import com.tnt.gateway.service.SessionService;
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
	public SignUpResponse finishSignUpWithImage(String profileImageUrl, Long memberId, MemberType memberType) {
		Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId)
			.orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));

		member.updateProfileImageUrl(profileImageUrl);

		String sessionId = String.valueOf(getTsid());

		sessionService.createSession(sessionId, String.valueOf(member.getId()));

		return new SignUpResponse(memberType, sessionId, member.getName(), member.getProfileImageUrl());
	}

	private void validateMemberNotExists(String socialId, SocialType socialType) {
		memberRepository.findBySocialIdAndSocialTypeAndDeletedAtIsNull(socialId, socialType)
			.ifPresent(member -> {
				throw new ConflictException(MEMBER_CONFLICT);
			});
	}

	private Long createTrainer(SignUpRequest request) {
		Member member = createMember(request, TRAINER_DEFAULT_IMAGE, TRAINER);
		Trainer trainer = Trainer.builder()
			.member(member)
			.build();

		trainerRepository.save(trainer);

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

		trainee = traineeRepository.save(trainee);

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

		return memberRepository.save(member);
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
