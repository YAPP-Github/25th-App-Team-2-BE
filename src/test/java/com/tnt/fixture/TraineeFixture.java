package com.tnt.fixture;

import com.tnt.domain.member.Member;
import com.tnt.domain.trainee.Trainee;

public final class TraineeFixture {

	public static Trainee getTrainee1(Long traineeId, Member member) {
		return Trainee.builder()
			.id(traineeId)
			.member(member)
			.height(180.4)
			.weight(70.5)
			.cautionNote("주의사항")
			.build();
	}

	public static Trainee getTrainee2(Member member) {
		return Trainee.builder()
			.member(member)
			.height(170.5)
			.weight(60.5)
			.cautionNote("주의사항")
			.build();
	}
}
