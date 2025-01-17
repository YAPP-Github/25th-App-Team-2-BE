package com.tnt.application.member;

import static com.tnt.global.error.model.ErrorMessage.MEMBER_CONFLICT;
import static com.tnt.global.error.model.ErrorMessage.UNSUPPORTED_MEMBER_TYPE;
import static io.micrometer.common.util.StringUtils.isNotBlank;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tnt.application.auth.SessionService;
import com.tnt.application.s3.S3Service;
import com.tnt.domain.member.Member;
import com.tnt.domain.member.SocialType;
import com.tnt.domain.trainee.PtGoal;
import com.tnt.domain.trainee.Trainee;
import com.tnt.domain.trainer.Trainer;
import com.tnt.dto.member.request.SignUpRequest;
import com.tnt.dto.member.response.SignUpResponse;
import com.tnt.global.error.exception.ConflictException;
import com.tnt.infrastructure.mysql.repository.member.MemberRepository;
import com.tnt.infrastructure.mysql.repository.trainee.PtGoalRepository;
import com.tnt.infrastructure.mysql.repository.trainee.TraineeRepository;
import com.tnt.infrastructure.mysql.repository.trainer.TrainerRepository;

import io.hypersistence.tsid.TSID;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

	private static final String TRAINER = "trainer";
	private static final String TRAINEE = "trainee";
	private static final String TRAINER_S3_PROFILE_PATH = "profiles/trainers";
	private static final String TRAINEE_S3_PROFILE_PATH = "profiles/trainees";
	private static final String TRAINER_DEFAULT_IMAGE = "https://tntapp-bucket.s3.ap-northeast-2.amazonaws.com/profiles/trainers/basic_profile_trainer.svg";
	private static final String TRAINEE_DEFAULT_IMAGE = "https://tntapp-bucket.s3.ap-northeast-2.amazonaws.com/profiles/trainees/basic_profile_trainee.svg";

	private final MemberRepository memberRepository;
	private final TrainerRepository trainerRepository;
	private final TraineeRepository traineeRepository;
	private final PtGoalRepository ptGoalRepository;
	private final S3Service s3Service;
	private final SessionService sessionService;

	public SignUpResponse signUp(SignUpRequest request, MultipartFile profileImage) {
		validateMemberNotExists(request.socialId(), request.socialType());

		Member member = createMember(request, profileImage);

		switch (request.memberType()) {
			case TRAINER -> createTrainer(member);
			case TRAINEE -> createTrainee(member, request);
			default -> throw new IllegalArgumentException(UNSUPPORTED_MEMBER_TYPE.getMessage());
		}

		String sessionId = String.valueOf(TSID.Factory.getTsid());

		sessionService.createOrUpdateSession(sessionId, String.valueOf(member.getId()));

		return new SignUpResponse(sessionId, member.getName(), member.getProfileImageUrl(), request.memberType());
	}

	private void validateMemberNotExists(String socialId, String socialType) {
		memberRepository.findBySocialIdAndSocialTypeAndDeletedAtIsNull(socialId, SocialType.valueOf(socialType))
			.ifPresent(member -> {
				throw new ConflictException(MEMBER_CONFLICT);
			});
	}

	private Member createMember(SignUpRequest request, MultipartFile profileImage) {
		Member member = Member.builder()
			.socialId(request.socialId())
			.fcmToken(request.fcmToken())
			.email(request.socialEmail())
			.name(request.name())
			.profileImageUrl(uploadProfileImage(profileImage, request.memberType()))
			.serviceAgreement(true)
			.collectionAgreement(true)
			.advertisementAgreement(true)
			.pushAgreement(true)
			.socialType(SocialType.valueOf(request.socialType()))
			.build();

		return memberRepository.save(member);
	}

	private void createTrainer(Member member) {
		Trainer trainer = Trainer.builder()
			.memberId(member.getId())
			.build();

		trainerRepository.save(trainer);
	}

	private void createTrainee(Member member, SignUpRequest request) {
		Trainee trainee = Trainee.builder()
			.memberId(member.getId())
			.height(request.height())
			.weight(request.weight())
			.cautionNote(isNotBlank(request.cautionNote()) ? request.cautionNote() : "")
			.build();

		trainee = traineeRepository.save(trainee);

		createPtGoals(trainee, request.goalContents());
	}

	private void createPtGoals(Trainee trainee, List<String> goalContents) {
		goalContents.forEach(content -> {
			PtGoal ptGoal = PtGoal.builder()
				.traineeId(trainee.getId())
				.content(content)
				.build();

			ptGoalRepository.save(ptGoal);
		});
	}

	private String uploadProfileImage(MultipartFile profileImage, String memberType) {
		String defaultImage;
		String folderPath;

		switch (memberType) {
			case TRAINER -> {
				defaultImage = TRAINER_DEFAULT_IMAGE;
				folderPath = TRAINER_S3_PROFILE_PATH;
			}
			case TRAINEE -> {
				defaultImage = TRAINEE_DEFAULT_IMAGE;
				folderPath = TRAINEE_S3_PROFILE_PATH;
			}
			default -> throw new IllegalArgumentException(UNSUPPORTED_MEMBER_TYPE.getMessage());
		}

		if (Objects.isNull(profileImage)) {
			return defaultImage;
		}

		try {
			return s3Service.uploadFile(profileImage, folderPath);
		} catch (Exception e) {
			return defaultImage;
		}
	}
}
