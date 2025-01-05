package com.tnt.domain.member;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hypersistence.tsid.TSID;

@ExtendWith(MockitoExtension.class)
class MemberTest {

	@Nested
	@DisplayName("회원 생성")
	class CreateMember {

		@Test
		@DisplayName("회원 생성 성공")
		void create_member_success() {
			// when
			Member member = Member.builder()
				.id(TSID.fast().toLong())  // TSID 직접 생성
				.socialId("12345")
				.email("test@example.com")
				.name("홍길동")
				.age("20")
				.socialType(SocialType.KAKAO)
				.build();

			// then
			assertThat(member.getId()).isNotNull();
			assertThat(String.valueOf(member.getId())).hasSize(18);
		}

		@Test
		@DisplayName("tsid 중복 검증 성공")
		void verify_tsid_duplication_success() {
			// when
			List<Long> ids = IntStream.range(0, 100)
				.mapToObj(i -> Member.builder()
					.id(TSID.fast().toLong())
					.socialId("social" + i)
					.email("test" + i + "@example.com")
					.name("사용자" + i)
					.age(String.valueOf(20 + (i % 20)))
					.socialType(SocialType.KAKAO)
					.build())
				.map(Member::getId)
				.toList();

			// then
			assertThat(ids).doesNotHaveDuplicates();

			// ID가 시간순으로 증가하는지 검사
			List<Long> sortedIds = new ArrayList<>(ids);
			Collections.sort(sortedIds);
			assertThat(ids).isEqualTo(sortedIds);
		}

		@Test
		@DisplayName("tsid의 타임스탬프가 현재 시간과 일치하는지 검증 성공")
		void verify_tsid_timestamp_success() {
			// when
			Member member = Member.builder()
				.id(TSID.fast().toLong())  // TSID 직접 생성
				.socialId("12345")
				.email("test@example.com")
				.name("홍길동")
				.age("20")
				.socialType(SocialType.KAKAO)
				.build();
			TSID tsid = TSID.from(member.getId());
			Instant timestamp = tsid.getInstant();

			// then
			assertThat(timestamp).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
		}
	}
}
