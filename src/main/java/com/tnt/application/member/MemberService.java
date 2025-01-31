package com.tnt.application.member;

import static com.tnt.common.error.model.ErrorMessage.MEMBER_CONFLICT;
import static com.tnt.common.error.model.ErrorMessage.MEMBER_NOT_FOUND;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tnt.application.s3.S3Service;
import com.tnt.common.error.exception.ConflictException;
import com.tnt.common.error.exception.NotFoundException;
import com.tnt.domain.member.Member;
import com.tnt.domain.member.SocialType;
import com.tnt.infrastructure.mysql.repository.member.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

	private final S3Service s3Service;
	private final MemberRepository memberRepository;

	public Member getMemberWithMemberId(Long memberId) {
		return memberRepository.findByIdAndDeletedAtIsNull(memberId)
			.orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
	}

	public Member getMemberWithSocialIdAndSocialType(String socialId, SocialType socialType) {
		return memberRepository.findBySocialIdAndSocialTypeAndDeletedAtIsNull(socialId,
			socialType).orElse(null);
	}

	public void validateMemberNotExists(String socialId, SocialType socialType) {
		memberRepository.findBySocialIdAndSocialTypeAndDeletedAtIsNull(socialId, socialType)
			.ifPresent(member -> {
				throw new ConflictException(MEMBER_CONFLICT);
			});
	}

	public Member saveMember(Member member) {
		return memberRepository.save(member);
	}

	@Transactional
	public void softDeleteMember(Member member) {
		LocalDateTime now = LocalDateTime.now();

		s3Service.deleteProfileImage(member.getProfileImageUrl());

		member.updateDeletedAt(now);
	}
}
