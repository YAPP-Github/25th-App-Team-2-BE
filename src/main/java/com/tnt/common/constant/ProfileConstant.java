package com.tnt.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileConstant {

	public static final String TRAINER_DEFAULT_IMAGE = "https://images.tntapp.co.kr/profiles/trainers/basic_trainer_image.png";
	public static final String TRAINEE_DEFAULT_IMAGE = "https://images.tntapp.co.kr/profiles/trainees/basic_trainee_image.png";
	public static final String TRAINER_S3_PROFILE_PATH = "profiles/trainers";
	public static final String TRAINEE_S3_PROFILE_PATH = "profiles/trainees";
}
