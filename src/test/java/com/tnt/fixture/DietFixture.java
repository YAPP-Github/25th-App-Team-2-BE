package com.tnt.fixture;

import static com.tnt.domain.trainee.DietType.BREAKFAST;
import static com.tnt.domain.trainee.DietType.DINNER;
import static com.tnt.domain.trainee.DietType.LUNCH;

import java.time.LocalDateTime;

import com.tnt.domain.trainee.Diet;

public final class DietFixture {

	public static Diet getDiet1(Long traineeId) {
		LocalDateTime date = LocalDateTime.parse("2025-02-01T11:38");
		String dietImageUrl = "test1.jpg";
		String memo = "배부름";

		return Diet.builder()
			.traineeId(traineeId)
			.date(date)
			.dietImageUrl(dietImageUrl)
			.dietType(BREAKFAST)
			.memo(memo)
			.build();
	}

	public static Diet getDiet2(Long traineeId) {
		LocalDateTime date = LocalDateTime.parse("2025-02-01T18:38");
		String dietImageUrl = "test2.jpg";
		String memo = "배고픔";

		return Diet.builder()
			.traineeId(traineeId)
			.date(date)
			.dietImageUrl(dietImageUrl)
			.dietType(DINNER)
			.memo(memo)
			.build();
	}

	public static Diet getDiet3(Long traineeId) {
		LocalDateTime date = LocalDateTime.parse("2025-02-02T12:38");
		String dietImageUrl = "test3.jpg";
		String memo = "모르겠음";

		return Diet.builder()
			.traineeId(traineeId)
			.date(date)
			.dietImageUrl(dietImageUrl)
			.dietType(LUNCH)
			.memo(memo)
			.build();
	}

	public static Diet getDiet4(Long traineeId) {
		LocalDateTime date = LocalDateTime.parse("2025-02-02T19:38");
		String dietImageUrl = "test4.jpg";
		String memo = "여전히";

		return Diet.builder()
			.traineeId(traineeId)
			.date(date)
			.dietImageUrl(dietImageUrl)
			.dietType(DINNER)
			.memo(memo)
			.build();
	}
}
