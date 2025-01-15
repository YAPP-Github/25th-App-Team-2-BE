package com.tnt.application.member;

import static com.tnt.global.error.model.ErrorMessage.MEMBER_CONFLICT;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tnt.domain.member.SocialType;
import com.tnt.dto.member.request.SignUpRequest;
import com.tnt.dto.member.response.SignUpResponse;
import com.tnt.global.error.exception.ConflictException;
import com.tnt.infrastructure.mysql.repository.member.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;

	@Transactional
	public SignUpResponse signUp(SignUpRequest request) {
		validateMemberNotExists(request.socialId(), request.socialType());

		return null;
	}

	private void validateMemberNotExists(String socialId, String socialType) {
		memberRepository.findBySocialIdAndSocialTypeAndDeletedAtIsNotNull(socialId, SocialType.valueOf(socialType))
			.ifPresent(member -> {
				throw new ConflictException(MEMBER_CONFLICT);
			});
	}
}
