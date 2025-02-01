package com.tnt.presentation.trainer;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tnt.application.pt.PtService;
import com.tnt.application.trainer.TrainerService;
import com.tnt.dto.trainer.response.ConnectWithTraineeResponse;
import com.tnt.dto.trainer.response.GetPtLessonsOnDateResponse;
import com.tnt.dto.trainer.response.InvitationCodeResponse;
import com.tnt.dto.trainer.response.InvitationCodeVerifyResponse;
import com.tnt.gateway.config.AuthMember;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "트레이너", description = "트레이너 관련 API")
@RestController
@RequestMapping("/trainers")
@RequiredArgsConstructor
public class TrainerController {

	private final TrainerService trainerService;
	private final PtService ptService;

	@Operation(summary = "트레이너 초대 코드 불러오기 API")
	@ResponseStatus(OK)
	@GetMapping("/invitation-code")
	public InvitationCodeResponse getInvitationCode(@AuthMember Long memberId) {
		return trainerService.getInvitationCode(memberId);
	}

	@Operation(summary = "트레이너 초대 코드 인증 API")
	@ResponseStatus(OK)
	@GetMapping("/invitation-code/verify/{code}")
	public InvitationCodeVerifyResponse verifyInvitationCode(@PathVariable("code") String code) {
		return trainerService.verifyInvitationCode(code);
	}

	@Operation(summary = "트레이너 초대 코드 재발급 API")
	@ResponseStatus(CREATED)
	@PutMapping("/invitation-code/reissue")
	public InvitationCodeResponse reissueInvitationCode(@AuthMember Long memberId) {
		return trainerService.reissueInvitationCode(memberId);
	}

	@Operation(summary = "연결 완료된 트레이니 최초로 정보 불러오기 API")
	@ResponseStatus(OK)
	@GetMapping("/first-connected-trainee")
	public ConnectWithTraineeResponse getFirstConnectedTrainee(@AuthMember Long memberId,
		@RequestParam("trainerId") Long trainerId, @RequestParam Long traineeId) {
		return ptService.getFirstTrainerTraineeConnect(memberId, trainerId, traineeId);
	}

	@Operation(summary = "특정 날짜의 PT 리스트 불러오기 API")
	@ResponseStatus(OK)
	@GetMapping("/lessons/{date}")
	public GetPtLessonsOnDateResponse getPtLessonsOnDate(@AuthMember Long memberId,
		@PathVariable("date") LocalDate date) {
		return ptService.getPtLessonsOnDate(memberId, date);
	}
}
