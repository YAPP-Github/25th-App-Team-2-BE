package com.tnt.fixture;

import static com.tnt.domain.member.MemberType.*;
import static com.tnt.domain.member.SocialType.*;

import java.time.LocalDate;

import com.tnt.domain.member.Member;

public final class MemberFixture {

	public static Member getTrainerMember1() {
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
			.socialType(KAKAO)
			.memberType(TRAINER)
			.build();
	}

	public static Member getTrainerMember1WithId() {
		String socialId = "1234567890";
		String email = "abc@gmail.com";
		String name = "김영명";
		String fcmToken = "fcmToken";
		LocalDate birthday = LocalDate.of(2023, 1, 1);
		String profileImageUrl = "https://profile.com/1234567890";

		return Member.builder()
			.id(1L)
			.socialId(socialId)
			.email(email)
			.name(name)
			.birthday(birthday)
			.fcmToken(fcmToken)
			.profileImageUrl(profileImageUrl)
			.serviceAgreement(true)
			.collectionAgreement(true)
			.advertisementAgreement(true)
			.socialType(KAKAO)
			.memberType(TRAINER)
			.build();
	}

	public static Member getTraineeMember1() {
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
			.socialType(KAKAO)
			.memberType(TRAINEE)
			.build();
	}

	public static Member getTraineeMember1WithId() {
		String socialId = "9876765541";
		String email = "wqert@gmail.com";
		String name = "조만제";
		String fcmToken = "fcmToken";
		LocalDate birthday = LocalDate.of(2019, 12, 12);
		String profileImageUrl = "https://profile.com/120847210";

		return Member.builder()
			.id(2L)
			.socialId(socialId)
			.email(email)
			.name(name)
			.fcmToken(fcmToken)
			.birthday(birthday)
			.profileImageUrl(profileImageUrl)
			.serviceAgreement(true)
			.collectionAgreement(true)
			.advertisementAgreement(true)
			.socialType(KAKAO)
			.memberType(TRAINEE)
			.build();
	}

	public static Member getTraineeMember2() {
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
			.socialType(KAKAO)
			.memberType(UNREGISTERED)
			.build();
	}

	public static Member getTraineeMember2WithId() {
		String socialId = "9876765541";
		String email = "wqert@gmail.com";
		String name = "조만제";
		String fcmToken = "fcmToken";
		LocalDate birthday = LocalDate.of(2019, 12, 12);
		String profileImageUrl = "https://profile.com/120847210";

		return Member.builder()
			.id(3L)
			.socialId(socialId)
			.email(email)
			.name(name)
			.fcmToken(fcmToken)
			.birthday(birthday)
			.profileImageUrl(profileImageUrl)
			.serviceAgreement(true)
			.collectionAgreement(true)
			.advertisementAgreement(true)
			.socialType(KAKAO)
			.memberType(UNREGISTERED)
			.build();
	}

	public static Member getTraineeMember3() {
		String socialId = "9876765540";
		String email = "zcsdf@gmail.com";
		String name = "홍길동";
		String fcmToken = "fcmToken";
		LocalDate birthday = LocalDate.of(2010, 12, 12);
		String profileImageUrl = "https://profile.com/120844510";

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
			.socialType(APPLE)
			.memberType(TRAINEE)
			.build();
	}

	public static Member getTraineeMember4() {
		String socialId = "76765540";
		String email = "bvfdf@gmail.com";
		String name = "김철수";
		String fcmToken = "fcmToken";
		LocalDate birthday = LocalDate.of(2000, 12, 12);
		String profileImageUrl = "https://profile.com/645344510";

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
			.socialType(APPLE)
			.memberType(TRAINEE)
			.build();
	}
}
