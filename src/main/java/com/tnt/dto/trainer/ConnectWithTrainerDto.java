package com.tnt.dto.trainer;

import com.tnt.dto.trainer.response.ConnectWithTrainerResponse;

public record ConnectWithTrainerDto(
	String trainerFcmToken,
	String trainerName,
	String traineeName,
	String trainerProfileImageUrl,
	String traineeProfileImageUrl
) {

	public ConnectWithTrainerResponse toResponse() {
		return new ConnectWithTrainerResponse(trainerName, traineeName, trainerProfileImageUrl, traineeProfileImageUrl);
	}
}
