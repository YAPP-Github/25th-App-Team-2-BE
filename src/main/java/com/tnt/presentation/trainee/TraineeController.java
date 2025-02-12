package com.tnt.presentation.trainee;

import static com.tnt.common.constant.ImageConstant.DIET_S3_IMAGE_PATH;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tnt.application.member.NotificationService;
import com.tnt.application.pt.PtService;
import com.tnt.application.s3.S3Service;
import com.tnt.dto.trainee.request.ConnectWithTrainerRequest;
import com.tnt.dto.trainee.request.CreateDietRequest;
import com.tnt.dto.trainee.response.ConnectWithTrainerResponse;
import com.tnt.dto.trainee.response.CreateDietResponse;
import com.tnt.dto.trainee.response.GetDietResponse;
import com.tnt.dto.trainee.response.GetTraineeCalendarPtLessonCountResponse;
import com.tnt.dto.trainer.ConnectWithTrainerDto;
import com.tnt.gateway.config.AuthMember;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "트레이니", description = "트레이니 관련 API")
@RestController
@RequestMapping("/trainees")
@RequiredArgsConstructor
public class TraineeController {

	private final PtService ptService;
	private final NotificationService notificationService;
	private final S3Service s3Service;

	@Operation(summary = "트레이너 연결 요청 API")
	@ResponseStatus(CREATED)
	@PostMapping("/connect-trainer")
	public ConnectWithTrainerResponse connectWithTrainer(@AuthMember Long memberId,
		@RequestBody @Valid ConnectWithTrainerRequest request) {
		ConnectWithTrainerDto connectWithTrainerDto = ptService.connectWithTrainer(memberId, request);
		notificationService.sendConnectNotificationToTrainer(connectWithTrainerDto.trainerFcmToken(),
			connectWithTrainerDto.traineeName(), connectWithTrainerDto.trainerId(), connectWithTrainerDto.traineeId());

		return connectWithTrainerDto.toResponse();
	}

	@Operation(summary = "트레이니 식단 등록 API")
	@ResponseStatus(CREATED)
	@PostMapping(value = "/diets", consumes = MULTIPART_FORM_DATA_VALUE)
	public CreateDietResponse createDiet(@AuthMember Long memberId,
		@RequestPart("request") @Valid CreateDietRequest request,
		@RequestPart(value = "dietImage", required = false) MultipartFile dietImage) {
		String dietImageUrl = s3Service.uploadImage(null, DIET_S3_IMAGE_PATH, dietImage);

		return ptService.createDiet(memberId, request, dietImageUrl);
	}

	@Operation(summary = "특정 식단 조회 API")
	@ResponseStatus(OK)
	@GetMapping("/diets/{dietId}")
	public GetDietResponse getDiet(@AuthMember Long memberId,
		@Parameter(description = "식단 ID", example = "12345") @PathVariable("dietId") Long dietId) {
		return ptService.getDiet(memberId, dietId);
	}

	@Operation(summary = "달력 PT 수업 있는 날 표시 데이터 조회 API")
	@ResponseStatus(OK)
	@GetMapping("/lessons/calendar")
	public GetTraineeCalendarPtLessonCountResponse getTraineeCalendarPtLessonCount(@AuthMember Long memberId,
		@Parameter(description = "조회 시작 날짜", example = "2025-01-10")
		@RequestParam("startDate") LocalDate startDate,
		@Parameter(description = "조회 종료 날짜", example = "2025-02-15")
		@RequestParam("endDate") LocalDate endDate) {
		return ptService.getTraineeCalendarPtLessonCount(memberId, startDate, endDate);
	}
}
