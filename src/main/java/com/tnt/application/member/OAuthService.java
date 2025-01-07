package com.tnt.application.member;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.tnt.domain.member.repository.MemberRepository;
import com.tnt.dto.member.request.OAuthLoginRequest;
import com.tnt.dto.member.response.OAuthLoginResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuthService {

	private static final String KAKAO = "kakao";
	private static final String APPLE = "apple";
	private final WebClient webClient;
	private final MemberRepository memberRepository;

	public OAuthLoginResponse oauthLogin(OAuthLoginRequest request) {

		return OAuthLoginResponse.from("");
	}
}
