package com.tnt.presentation.trainee;

import static org.springframework.http.HttpStatus.CREATED;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tnt.application.member.NotificationService;
import com.tnt.application.pt.PtService;
import com.tnt.dto.trainer.ConnectWithTrainerDto;
import com.tnt.dto.trainer.request.ConnectWithTrainerRequest;
import com.tnt.dto.trainer.response.ConnectWithTrainerResponse;
import com.tnt.global.auth.annotation.AuthMember;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "트레이니", description = "트레이니 관련 API")
@RestController
@RequestMapping("/trainees")
@RequiredArgsConstructor
public class TraineeController {

	private final PtService ptService;
	private final NotificationService notificationService;

	@Operation(summary = "트레이너 연결 요청 API")
	@ResponseStatus(CREATED)
	@PostMapping("/connect-trainer")
	public ConnectWithTrainerResponse connectWithTrainer(@AuthMember String memberId,
		@RequestBody ConnectWithTrainerRequest request) {
		ConnectWithTrainerDto connectWithTrainerDto = ptService.connectWithTrainer(memberId, request);
		notificationService.sendConnectNotificationToTrainer(connectWithTrainerDto.trainerFcmToken(),
			connectWithTrainerDto.traineeName(), connectWithTrainerDto.trainerId(), connectWithTrainerDto.traineeId());

		return connectWithTrainerDto.toResponse();
	}
}
