package com.tnt.presentation.trainer;

import static org.springframework.http.HttpStatus.*;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tnt.application.trainer.TrainerService;
import com.tnt.dto.trainer.response.InvitationCodeResponse;
import com.tnt.global.auth.annotation.AuthMember;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "트레이너", description = "트레이너 관련 API")
@RestController
@RequestMapping("/trainers")
@RequiredArgsConstructor
public class TrainerController {

	private final TrainerService trainerService;

	@Operation(summary = "트레이너 초대 코드 불러오기 API")
	@ResponseStatus(OK)
	@GetMapping("/invitation-code")
	public InvitationCodeResponse getInvitationCode(@AuthMember String memberId) {
		return trainerService.getInvitationCode(memberId);
	}

	@Operation(summary = "트레이너 초대 코드 재발급 API")
	@ResponseStatus(CREATED)
	@PutMapping("/invitation-code")
	public InvitationCodeResponse reissueInvitationCode(@AuthMember String memberId) {
		return trainerService.reissueInvitationCode(memberId);
	}
}
