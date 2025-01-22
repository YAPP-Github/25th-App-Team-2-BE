package com.tnt.presentation.member;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tnt.application.member.MemberService;
import com.tnt.application.s3.S3Service;
import com.tnt.dto.member.request.SignUpRequest;
import com.tnt.dto.member.response.SignUpResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "회원", description = "회원 관련 API")
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;
	private final S3Service s3Service;

	@Operation(summary = "회원가입 API")
	@PostMapping(value = "/sign-up", consumes = MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(value = OK)
	public SignUpResponse signUp(@RequestPart(value = "request") @Valid SignUpRequest request,
		@RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
		Long memberId = memberService.signUp(request);
		String profileImageUrl = s3Service.uploadProfileImage(profileImage, request.memberType());

		return memberService.finishSignUpWithImage(profileImageUrl, memberId, request.memberType());
	}
}
