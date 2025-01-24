package com.tnt.domain.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constant {

	public static final String KAKAO = "KAKAO";
	public static final String APPLE = "APPLE";
	public static final String TRAINER = "trainer";
	public static final String TRAINEE = "trainee";
	public static final String TRAINER_DEFAULT_IMAGE = "https://images.tntapp.co.kr/profiles/trainers/basic_profile_trainer.svg";
	public static final String TRAINEE_DEFAULT_IMAGE = "https://images.tntapp.co.kr/profiles/trainees/basic_profile_trainee.svg";
	public static final String TRAINER_S3_PROFILE_PATH = "profiles/trainers";
	public static final String TRAINEE_S3_PROFILE_PATH = "profiles/trainees";
}
