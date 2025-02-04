package com.tnt.gateway.service;

import static com.tnt.common.error.model.ErrorMessage.APPLE_AUTH_ERROR;
import static com.tnt.common.error.model.ErrorMessage.APPLE_SERVER_ERROR;
import static com.tnt.common.error.model.ErrorMessage.KAKAO_SERVER_ERROR;
import static com.tnt.common.error.model.ErrorMessage.MATCHING_KEY_NOT_FOUND;
import static com.tnt.domain.member.MemberType.UNREGISTERED;
import static io.hypersistence.tsid.TSID.Factory.getTsid;
import static java.util.Objects.isNull;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.tnt.application.member.MemberService;
import com.tnt.common.error.exception.NotFoundException;
import com.tnt.common.error.exception.OAuthException;
import com.tnt.common.error.model.ErrorMessage;
import com.tnt.domain.member.Member;
import com.tnt.dto.member.response.LogoutResponse;
import com.tnt.gateway.dto.AppleUserInfo;
import com.tnt.gateway.dto.KakaoUserInfo;
import com.tnt.gateway.dto.OAuthUserInfo;
import com.tnt.gateway.dto.request.OAuthLoginRequest;
import com.tnt.gateway.dto.response.OAuthLoginResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OAuthService {

	private final WebClient webClient;
	private final SessionService sessionService;
	private final MemberService memberService;

	@Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
	private String kakaoApiUrl;

	@Value("${social-login.provider.kakao.unlink-uri}")
	private String kakaoUnlinkUrl;

	@Value("${social-login.provider.apple.audience}")
	private String appleApiUrl;

	@Value("${social-login.provider.apple.team-id}")
	private String teamId;

	@Value("${social-login.provider.apple.client-id}")
	private String clientId;

	@Value("${social-login.provider.apple.key-id}")
	private String keyId;

	@Value("${social-login.provider.apple.private-key}")
	private String privateKey;

	@Transactional
	public OAuthLoginResponse oauthLogin(OAuthLoginRequest request) {
		OAuthUserInfo oauthInfo = extractOAuthUserInfo(request);
		String socialId = oauthInfo.getId();
		String socialEmail = oauthInfo.getEmail();
		Member member;

		try {
			member = memberService.getMemberWithSocialIdAndSocialType(socialId, request.socialType());
		} catch (NotFoundException e) {
			return new OAuthLoginResponse(null, socialId, socialEmail, request.socialType(), false, UNREGISTERED);
		}

		member.updateFcmTokenIfExpired(request.fcmToken());

		String sessionId = String.valueOf(getTsid());

		sessionService.createSession(sessionId, String.valueOf(member.getId()));

		return new OAuthLoginResponse(sessionId, member.getSocialId(), member.getEmail(), member.getSocialType(), true,
			member.getMemberType());
	}

	public LogoutResponse logout(Long memberId) {
		String removeSessionId = sessionService.removeSession(String.valueOf(memberId));

		return new LogoutResponse(removeSessionId);
	}

	private OAuthUserInfo extractOAuthUserInfo(OAuthLoginRequest request) {
		return switch (request.socialType()) {
			case KAKAO -> new KakaoUserInfo(handleKakaoLogin(request.socialAccessToken()));
			case APPLE -> new AppleUserInfo(handleAppleLogin(request));
		};
	}

	private Map<String, Object> handleKakaoLogin(String socialAccessToken) {
		return webClient
			.get()
			.uri(kakaoApiUrl)
			.headers(headers -> headers.setBearerAuth(socialAccessToken))
			.retrieve()
			.onStatus(HttpStatusCode::isError, response -> handleErrorResponse(response, KAKAO_SERVER_ERROR))
			.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
			})
			.block();
	}

	private Map<String, Object> handleAppleLogin(OAuthLoginRequest request) {
		String idToken = request.idToken();

		try {
			// Apple의 공개키 가져오기
			String jwksJson = fetchApplePublicKeys();
			JSONObject jwks = new JSONObject(jwksJson);
			JSONArray keys = jwks.getJSONArray("keys");

			// ID 토큰 파싱
			String[] tokenParts = idToken.split("\\.");
			String header = new String(Base64.getUrlDecoder().decode(tokenParts[0]));
			String payload = new String(Base64.getUrlDecoder().decode(tokenParts[1]));
			JSONObject headerJson = new JSONObject(header);
			String kid = headerJson.getString("kid");

			// 매칭되는 키 찾기
			JSONObject key = findMatchingKey(keys, kid);

			if (isNull(key)) {
				throw new OAuthException(MATCHING_KEY_NOT_FOUND);
			}

			RSAPublicKey publicKey = getRsaPublicKey(key);

			// 토큰 검증
			verifyToken(idToken, publicKey);

			// 페이로드에서 사용자 정보 추출
			return extractUserInfo(payload);
		} catch (Exception e) {
			throw new OAuthException(APPLE_AUTH_ERROR);
		}
	}

	private Mono<? extends Throwable> handleErrorResponse(ClientResponse response, ErrorMessage errorMessage) {
		return response.bodyToMono(String.class)
			.flatMap(body -> Mono.error(new OAuthException(errorMessage)));
	}

	private String fetchApplePublicKeys() {
		return webClient.get()
			.uri(appleApiUrl + "/auth/keys")
			.retrieve()
			.onStatus(HttpStatusCode::isError, response -> handleErrorResponse(response, APPLE_SERVER_ERROR))
			.bodyToMono(String.class)
			.block();
	}

	private JSONObject findMatchingKey(JSONArray keys, String kid) {
		for (int i = 0; i < keys.length(); i++) {
			JSONObject key = keys.getJSONObject(i);

			if (kid.equals(key.getString("kid"))) {
				return key;
			}
		}

		return null;
	}

	private RSAPublicKey getRsaPublicKey(JSONObject key) throws NoSuchAlgorithmException, InvalidKeySpecException {
		BigInteger nInt = new BigInteger(1, Base64.getUrlDecoder().decode(key.getString("n")));
		BigInteger eInt = new BigInteger(1, Base64.getUrlDecoder().decode(key.getString("e")));
		RSAPublicKeySpec spec = new RSAPublicKeySpec(nInt, eInt);
		KeyFactory factory = KeyFactory.getInstance("RSA");

		return (RSAPublicKey)factory.generatePublic(spec);
	}

	private void verifyToken(String idToken, RSAPublicKey publicKey) {
		Algorithm algorithm = Algorithm.RSA256(publicKey, null);
		JWTVerifier verifier = JWT.require(algorithm)
			.withIssuer(appleApiUrl)
			.build();

		verifier.verify(idToken);
	}

	private Map<String, Object> extractUserInfo(String payload) {
		JSONObject payloadJson = new JSONObject(payload);
		Map<String, Object> userInfo = new HashMap<>();
		String email = "email";

		userInfo.put("sub", payloadJson.getString("sub"));

		if (payloadJson.has(email)) {
			userInfo.put(email, payloadJson.getString(email));
		}

		return userInfo;
	}
}
