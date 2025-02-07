package com.tnt.application.member;

import static com.tnt.common.error.model.ErrorMessage.*;
import static com.tnt.dto.member.MemberProjection.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tnt.common.error.exception.ConflictException;
import com.tnt.common.error.exception.NotFoundException;
import com.tnt.domain.member.Member;
import com.tnt.domain.member.SocialType;
import com.tnt.dto.member.response.GetMemberInfoResponse;
import com.tnt.gateway.dto.response.CheckSessionResponse;
import com.tnt.infrastructure.mysql.repository.member.MemberRepository;
import com.tnt.infrastructure.mysql.repository.member.MemberSearchRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final MemberSearchRepository memberSearchRepository;

	@Transactional
	public Member saveMember(Member member) {
		return memberRepository.save(member);
	}

	public void validateMemberNotExists(String socialId, SocialType socialType) {
		memberRepository.findBySocialIdAndSocialTypeAndDeletedAtIsNull(socialId, socialType)
			.ifPresent(member -> {
				throw new ConflictException(MEMBER_CONFLICT);
			});
	}

	public GetMemberInfoResponse getMemberInfo(Long memberId) {
		MemberInfoDto memberInfo = memberSearchRepository.findMemberInfo(memberId)
			.orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));

		return new GetMemberInfoResponse(memberInfo.name(), memberInfo.email(), memberInfo.profileImageUrl(),
			memberInfo.birthday(), memberInfo.memberType(), memberInfo.socialType(), memberInfo.managementMember(),
			memberInfo.fellowMember(), memberInfo.invitationCode(), memberInfo.trainerId(), memberInfo.height(),
			memberInfo.weight(), memberInfo.cautionNote(), memberInfo.goalContents());
	}

	public CheckSessionResponse getMemberType(Long memberId) {
		MemberTypeDto memberTypeDto = memberSearchRepository.findMemberType(memberId)
			.orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));

		return new CheckSessionResponse(memberTypeDto.memberType());
	}

	public Member getMemberWithMemberId(Long memberId) {
		return memberRepository.findByIdAndDeletedAtIsNull(memberId)
			.orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
	}

	public Member getMemberWithSocialIdAndSocialType(String socialId, SocialType socialType) {
		return memberRepository.findBySocialIdAndSocialTypeAndDeletedAtIsNull(socialId, socialType)
			.orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
	}
}
