package com.tnt.fixture;

import com.tnt.domain.member.Member;
import com.tnt.domain.trainee.Trainee;

public final class TraineeFixture {

	public static Trainee getTrainee1WithId(Long traineeId, Member member) {
		return Trainee.builder()
			.id(traineeId)
			.member(member)
			.height(180.4)
			.weight(70.5)
			.cautionNote("주의사항00")
			.build();
	}

	public static Trainee getTrainee1(Member member) {
		return Trainee.builder()
			.member(member)
			.height(170.5)
			.weight(60.5)
			.cautionNote("주의사항11")
			.build();
	}

	public static Trainee getTrainee2(Member member) {
		return Trainee.builder()
			.member(member)
			.height(173.3)
			.weight(65.5)
			.cautionNote("주의사항22")
			.build();
	}
}
