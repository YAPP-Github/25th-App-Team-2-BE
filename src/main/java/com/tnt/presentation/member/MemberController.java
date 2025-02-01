package com.tnt.presentation.member;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tnt.application.member.SignUpService;
import com.tnt.application.member.WithdrawService;
import com.tnt.application.s3.S3Service;
import com.tnt.dto.member.WithdrawDto;
import com.tnt.dto.member.request.SignUpRequest;
import com.tnt.dto.member.request.WithdrawRequest;
import com.tnt.dto.member.response.SignUpResponse;
import com.tnt.gateway.config.AuthMember;
import com.tnt.gateway.service.OAuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "회원", description = "회원 관련 API")
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

	private final S3Service s3Service;
	private final SignUpService signUpService;
	private final WithdrawService withdrawService;
	private final OAuthService oAuthService;

	@Operation(summary = "회원가입 API")
	@PostMapping(value = "/sign-up", consumes = MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(CREATED)
	public SignUpResponse signUp(@RequestPart("request") @Valid SignUpRequest request,
		@RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
		Long memberId = signUpService.signUp(request);
		String profileImageUrl = s3Service.uploadProfileImage(profileImage, request.memberType());

		return signUpService.finishSignUpWithImage(profileImageUrl, memberId, request.memberType());
	}

	@Operation(summary = "회원탈퇴 API")
	@PostMapping("/withdraw")
	@ResponseStatus(OK)
	public void withdraw(@AuthMember Long memberId, @RequestBody @Valid WithdrawRequest request) {
		WithdrawDto withdrawDto = withdrawService.withdraw(memberId);

		s3Service.deleteProfileImage(withdrawDto.profileImageUrl());
		oAuthService.revoke(withdrawDto.socialId(), withdrawDto.socialType(), request);
	}
}
