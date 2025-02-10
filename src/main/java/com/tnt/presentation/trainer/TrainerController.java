package com.tnt.presentation.trainer;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tnt.application.pt.PtService;
import com.tnt.application.trainer.TrainerService;
import com.tnt.dto.trainer.request.CreatePtLessonRequest;
import com.tnt.dto.trainer.response.ConnectWithTraineeResponse;
import com.tnt.dto.trainer.response.GetActiveTraineesResponse;
import com.tnt.dto.trainer.response.GetCalendarPtLessonCountResponse;
import com.tnt.dto.trainer.response.GetPtLessonsOnDateResponse;
import com.tnt.dto.trainer.response.InvitationCodeResponse;
import com.tnt.dto.trainer.response.InvitationCodeVerifyResponse;
import com.tnt.gateway.config.AuthMember;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
		@RequestParam("trainerId") Long trainerId, @RequestParam("traineeId") Long traineeId) {
		return ptService.getFirstTrainerTraineeConnect(memberId, trainerId, traineeId);
	}

	@Operation(summary = "특정 날짜의 PT 리스트 불러오기 API")
	@ResponseStatus(OK)
	@GetMapping("/lessons/{date}")
	public GetPtLessonsOnDateResponse getPtLessonsOnDate(@AuthMember Long memberId,
		@Parameter(description = "날짜", example = "2025-01-03") @PathVariable("date") LocalDate date) {
		return ptService.getPtLessonsOnDate(memberId, date);
	}

	@Operation(summary = "달력 스케쥴 개수 표시에 필요한 데이터 요청 API")
	@ResponseStatus(OK)
	@GetMapping("/lessons/calendar")
	public GetCalendarPtLessonCountResponse getCalendarPtLessonCount(@AuthMember Long memberId,
		@Parameter(description = "년도", example = "2021") @RequestParam("year") @Min(1900) @Max(2100) Integer year,
		@Parameter(description = "월", example = "3") @RequestParam("month") @Min(1) @Max(12) Integer month) {
		return ptService.getCalendarPtLessonCount(memberId, year, month);
	}

	@Operation(summary = "PT 수업 추가 API")
	@ResponseStatus(CREATED)
	@PostMapping("/lessons")
	public void addPtLesson(@AuthMember Long memberId, @RequestBody @Valid CreatePtLessonRequest request) {
		ptService.addPtLesson(memberId, request);
	}

	@Operation(summary = "관리중인 회원 목록 요청 API")
	@ResponseStatus(OK)
	@GetMapping("/active-trainees")
	public GetActiveTraineesResponse getActiveTrainees(@AuthMember Long memberId) {
		return ptService.getActiveTrainees(memberId);
	}
}
