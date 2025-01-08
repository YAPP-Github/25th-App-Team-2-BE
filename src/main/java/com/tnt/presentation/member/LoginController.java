package com.tnt.presentation.member;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tnt.application.member.OAuthService;
import com.tnt.dto.member.request.OAuthLoginRequest;
import com.tnt.dto.member.response.OAuthLoginResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "로그인", description = "로그인 관련 API")
@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginController {

	private final OAuthService oauthService;

	@Operation(summary = "소셜 로그인")
	@PostMapping("/oauth")
	@ResponseStatus(value = HttpStatus.OK)
	public OAuthLoginResponse oauthLogin(@RequestBody @Valid OAuthLoginRequest request) {
		return oauthService.oauthLogin(request);
	}
}
