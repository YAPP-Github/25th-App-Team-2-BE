package com.tnt.application.member;

import static com.tnt.domain.member.SocialType.KAKAO;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tnt.application.s3.S3Service;
import com.tnt.common.error.exception.ConflictException;
import com.tnt.common.error.exception.NotFoundException;
import com.tnt.domain.member.Member;
import com.tnt.domain.member.SocialType;
import com.tnt.fixture.MemberFixture;
import com.tnt.infrastructure.mysql.repository.member.MemberRepository;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

	@Mock
	private S3Service s3Service;

	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private MemberService memberService;

	@Test
	@DisplayName("memberId로 회원 조회 성공")
	void get_member_with_member_id_success() {
		// given
		Member trainerMember = MemberFixture.getTrainerMember1WithId();

		given(memberRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.ofNullable(trainerMember));

		// when
		Member result = memberService.getMemberWithMemberId(requireNonNull(trainerMember).getId());

		// then
		assertThat(result).isNotNull().isEqualTo(trainerMember);
		verify(memberRepository).findByIdAndDeletedAtIsNull(1L);
	}

	@Test
	@DisplayName("존재하지 않는 memberId로 회원 조회시 실패")
	void get_member_with_member_id_error() {
		// given
		Long memberId = 999L;

		given(memberRepository.findByIdAndDeletedAtIsNull(999L)).willReturn(Optional.empty());

		// when & then
		assertThrows(NotFoundException.class, () -> memberService.getMemberWithMemberId(memberId));
		verify(memberRepository).findByIdAndDeletedAtIsNull(999L);
	}

	@Test
	@DisplayName("socialId와 socialType으로 회원 조회 성공")
	void get_member_with_social_id_and_type_success() {
		// given
		Member member = MemberFixture.getTrainerMember1WithId();
		String socialId = member.getSocialId();
		SocialType socialType = member.getSocialType();

		given(memberRepository.findBySocialIdAndSocialTypeAndDeletedAtIsNull(socialId, socialType)).willReturn(
			Optional.of(member));

		// when
		Member result = memberService.getMemberWithSocialIdAndSocialType(socialId, socialType);

		// then
		assertThat(result).isNotNull().isEqualTo(member);
		verify(memberRepository).findBySocialIdAndSocialTypeAndDeletedAtIsNull(socialId, socialType);
	}

	@Test
	@DisplayName("존재하지 않는 social 정보로 조회시 null 반환 성공")
	void get_member_with_social_id_and_type_return_null_success() {
		// given
		String socialId = "non";
		SocialType socialType = KAKAO;

		given(memberRepository.findBySocialIdAndSocialTypeAndDeletedAtIsNull(socialId, socialType)).willReturn(
			Optional.empty());

		// when
		Member result = memberService.getMemberWithSocialIdAndSocialType(socialId, socialType);

		// then
		assertThat(result).isNull();
		verify(memberRepository).findBySocialIdAndSocialTypeAndDeletedAtIsNull(socialId, socialType);
	}

	@Test
	@DisplayName("회원 중복 검증 성공")
	void validate_member_not_exists_success() {
		// given
		String socialId = "user";
		SocialType socialType = KAKAO;

		given(memberRepository.findBySocialIdAndSocialTypeAndDeletedAtIsNull(socialId, socialType)).willReturn(
			Optional.empty());

		// when & then
		assertDoesNotThrow(() -> memberService.validateMemberNotExists(socialId, socialType));
		verify(memberRepository).findBySocialIdAndSocialTypeAndDeletedAtIsNull(socialId, socialType);
	}

	@Test
	@DisplayName("이미 존재하는 회원 검증시 실패")
	void validate_member_exists_error() {
		// given
		Member existingMember = MemberFixture.getTrainerMember1WithId();
		String socialId = existingMember.getSocialId();
		SocialType socialType = existingMember.getSocialType();

		given(memberRepository.findBySocialIdAndSocialTypeAndDeletedAtIsNull(socialId, socialType)).willReturn(
			Optional.of(existingMember));

		// when & then
		assertThrows(ConflictException.class, () -> memberService.validateMemberNotExists(socialId, socialType));
		verify(memberRepository).findBySocialIdAndSocialTypeAndDeletedAtIsNull(socialId, socialType);
	}

	@Test
	@DisplayName("회원 저장 성공")
	void save_member_success() {
		// given
		Member member = MemberFixture.getTrainerMember1WithId();

		given(memberRepository.save(member)).willReturn(member);

		// when
		Member savedMember = memberService.saveMember(member);

		// then
		assertThat(savedMember).isNotNull().isEqualTo(member);
		verify(memberRepository).save(member);
	}

	@Test
	@DisplayName("회원 soft delete 성공")
	void soft_delete_member_success() {
		// given
		Member member = MemberFixture.getTrainerMember1WithId();

		// when
		memberService.softDeleteMember(member);

		// then
		assertThat(member.getDeletedAt()).isNotNull();
	}
}
