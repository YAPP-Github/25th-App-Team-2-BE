package com.tnt.presentation.member;

import static org.springframework.http.HttpStatus.OK;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tnt.application.member.OAuthService;
import com.tnt.dto.member.request.OAuthLoginRequest;
import com.tnt.dto.member.response.OAuthLoginResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "로그인/로그아웃", description = "로그인/로그아웃 관련 API")
@RestController
@RequiredArgsConstructor
public class AuthenticationController {

	private final OAuthService oauthService;

	@Operation(summary = "소셜 로그인 API")
	@PostMapping("/login")
	@ResponseStatus(value = OK)
	public OAuthLoginResponse oauthLogin(@RequestBody @Valid OAuthLoginRequest request) {
		return oauthService.oauthLogin(request);
	}
}
