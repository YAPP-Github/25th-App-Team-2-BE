package com.tnt.domain.member;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.tnt.infrastructure.mysql.repository.member.MemberRepository;

@SpringBootTest
@Transactional
class MemberIntegrationTest {

	@Autowired
	private MemberRepository memberRepository;

	@Test
	@DisplayName("회원 DB에 저장 시 tsid 자동 생성 성공")
	void save_member_to_db_success() {
		// given
		Member member = Member.builder()
			.socialId("12345")
			.email("test@example.com")
			.name("홍길동")
			.age(20)
			.socialType(SocialType.KAKAO)
			.build();

		// when
		Member savedMember = memberRepository.save(member);

		// then
		Member foundMember = memberRepository.findById(savedMember.getId())
			.orElseThrow();

		assertThat(foundMember.getId()).isNotNull();
		assertThat(String.valueOf(foundMember.getId())).hasSize(18);
		assertThat(foundMember.getId()).isEqualTo(savedMember.getId());
		assertThat(foundMember.getSocialId()).isEqualTo("12345");
		assertThat(foundMember.getEmail()).isEqualTo("test@example.com");
	}
}
