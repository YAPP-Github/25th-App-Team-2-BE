package com.tnt.fixture;

import static com.tnt.domain.trainee.DietType.DINNER;

import java.time.LocalDateTime;

import com.tnt.domain.trainee.Diet;

public final class DietFixture {

	public static Diet getDiet1(Long traineeId) {
		LocalDateTime date = LocalDateTime.parse("2025-02-11T15:38");
		String dietImageUrl = "test1.jpg";
		String memo = "배부름";

		return Diet.builder()
			.traineeId(traineeId)
			.date(date)
			.dietImageUrl(dietImageUrl)
			.dietType(DINNER)
			.memo(memo)
			.build();
	}
}
