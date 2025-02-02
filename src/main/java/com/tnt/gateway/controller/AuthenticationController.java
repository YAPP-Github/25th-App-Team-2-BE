package com.tnt.gateway.controller;

import static org.springframework.http.HttpStatus.OK;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tnt.application.member.MemberService;
import com.tnt.dto.member.response.LogoutResponse;
import com.tnt.gateway.config.AuthMember;
import com.tnt.gateway.dto.request.OAuthLoginRequest;
import com.tnt.gateway.dto.response.CheckSessionResponse;
import com.tnt.gateway.dto.response.OAuthLoginResponse;
import com.tnt.gateway.service.OAuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "로그인/로그아웃", description = "로그인/로그아웃 관련 API")
@RestController
@RequiredArgsConstructor
public class AuthenticationController {

	private final OAuthService oauthService;
	private final MemberService memberService;

	@Operation(summary = "소셜 로그인 API")
	@PostMapping("/login")
	@ResponseStatus(OK)
	public OAuthLoginResponse oauthLogin(@RequestBody @Valid OAuthLoginRequest request) {
		return oauthService.oauthLogin(request);
	}

	@Operation(summary = "로그아웃 API")
	@PostMapping("/logout")
	@ResponseStatus(OK)
	public LogoutResponse logout(@AuthMember Long memberId) {
		return oauthService.logout(memberId);
	}

	@Operation(summary = "로그인 세션 유효 확인 API")
	@GetMapping("/check-session")
	@ResponseStatus(OK)
	public CheckSessionResponse checkSession(@AuthMember Long memberId) {
		return memberService.getMemberType(memberId);
	}
}
