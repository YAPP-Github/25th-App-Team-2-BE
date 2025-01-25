package com.tnt.fixture;

import java.time.LocalDate;

import com.tnt.domain.member.Member;
import com.tnt.domain.member.SocialType;

public final class MemberFixture {

	public static Member getMember1() {
		String socialId = "1234567890";
		String email = "abc@gmail.com";
		String name = "김영명";
		String fcmToken = "fcmToken";
		LocalDate birthday = LocalDate.of(2023, 1, 1);
		String profileImageUrl = "https://profile.com/1234567890";

		return Member.builder()
			.socialId(socialId)
			.email(email)
			.name(name)
			.birthday(birthday)
			.fcmToken(fcmToken)
			.profileImageUrl(profileImageUrl)
			.serviceAgreement(true)
			.collectionAgreement(true)
			.advertisementAgreement(true)
			.pushAgreement(true)
			.socialType(SocialType.KAKAO)
			.build();
	}

	public static Member getMember2() {
		String socialId = "9876765541";
		String email = "wqert@gmail.com";
		String name = "조만제";
		String fcmToken = "fcmToken";
		LocalDate birthday = LocalDate.of(2019, 12, 12);
		String profileImageUrl = "https://profile.com/120847210";

		return Member.builder()
			.socialId(socialId)
			.email(email)
			.name(name)
			.fcmToken(fcmToken)
			.birthday(birthday)
			.profileImageUrl(profileImageUrl)
			.serviceAgreement(true)
			.collectionAgreement(true)
			.advertisementAgreement(true)
			.pushAgreement(true)
			.socialType(SocialType.KAKAO)
			.build();
	}
}
