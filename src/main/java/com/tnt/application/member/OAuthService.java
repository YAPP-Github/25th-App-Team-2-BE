package com.tnt.application.member;

import static com.tnt.domain.constant.Constant.APPLE;
import static com.tnt.domain.constant.Constant.KAKAO;
import static com.tnt.global.error.model.ErrorMessage.*;
import static java.util.Objects.isNull;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.tnt.application.auth.SessionService;
import com.tnt.domain.member.Member;
import com.tnt.domain.member.SocialType;
import com.tnt.dto.member.AppleUserInfo;
import com.tnt.dto.member.KakaoUserInfo;
import com.tnt.dto.member.OAuthUserInfo;
import com.tnt.dto.member.request.OAuthLoginRequest;
import com.tnt.dto.member.response.OAuthLoginResponse;
import com.tnt.global.error.exception.OAuthException;
import com.tnt.global.error.model.ErrorMessage;
import com.tnt.infrastructure.mysql.repository.member.MemberRepository;

import io.hypersistence.tsid.TSID;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OAuthService {

	private final WebClient webClient;
	private final SessionService sessionService;
	private final MemberRepository memberRepository;

	@Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
	private String kakaoApiUrl;

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

	public OAuthLoginResponse oauthLogin(OAuthLoginRequest request) {
		OAuthUserInfo oauthInfo = extractOAuthUserInfo(request);
		String socialId = oauthInfo.getId();
		String socialEmail = oauthInfo.getEmail();
		Member findMember = findMemberFromDB(socialId, request.socialType());

		if (isNull(findMember)) {
			return new OAuthLoginResponse(null, socialId, socialEmail, request.socialType(), false);
		}

		findMember.updateFcmTokenIfExpired(request.fcmToken());
		memberRepository.save(findMember);

		String sessionId = String.valueOf(TSID.Factory.getTsid());

		sessionService.createOrUpdateSession(sessionId, String.valueOf(findMember.getId()));

		return new OAuthLoginResponse(sessionId, null, null, null, true);
	}

	private OAuthUserInfo extractOAuthUserInfo(OAuthLoginRequest request) {
		return switch (request.socialType()) {
			case KAKAO -> new KakaoUserInfo(handleKakaoLogin(request.socialAccessToken()));
			case APPLE -> new AppleUserInfo(handleAppleLogin(request));
			default -> throw new OAuthException(UNSUPPORTED_SOCIAL_TYPE);
		};
	}

	private Map<String, Object> handleKakaoLogin(String socialAccessToken) {
		return webClient
			.get()
			.uri(kakaoApiUrl)
			.headers(headers -> headers.setBearerAuth(socialAccessToken))
			.retrieve()
			.onStatus(HttpStatusCode::isError, response -> handleErrorResponse(response, FAILED_TO_FETCH_USER_INFO))
			.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
			})
			.block();
	}

	private Map<String, Object> handleAppleLogin(OAuthLoginRequest request) {
		String idToken = Optional.ofNullable(request.idToken()) // Android
			.orElseGet(() -> getAppleIdToken(request.authorizationCode())); // iOS

		if (isNull(idToken)) {
			throw new OAuthException(APPLE_AUTH_ERROR);
		}

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

	// Apple 서버에 authorizationCode를 사용하여 idToken을 요청
	private String getAppleIdToken(String authorizationCode) {
		String clientSecret = generateClientSecret();

		return webClient.post()
			.uri(appleApiUrl + "/auth/token")
			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
			.body(createAuthRequestBody(authorizationCode, clientSecret))
			.retrieve()
			.onStatus(HttpStatusCode::is4xxClientError, response -> handleErrorResponse(response, APPLE_CLIENT_ERROR))
			.onStatus(HttpStatusCode::is5xxServerError, response -> handleErrorResponse(response, APPLE_SERVER_ERROR))
			.bodyToMono(Map.class)
			.map(response -> (String)response.get("id_token"))
			.block();
	}

	// JWT 형식의 클라이언트 시크릿 생성
	private String generateClientSecret() {
		try {
			// JWT 헤더 설정
			Map<String, Object> headerClaims = new HashMap<>();
			headerClaims.put("kid", keyId);

			// JWT 생성
			return JWT.create()
				.withHeader(headerClaims)
				.withIssuer(teamId)
				.withIssuedAt(new Date())
				.withExpiresAt(new Date(System.currentTimeMillis() + 15777000000L)) // 6개월
				.withAudience(appleApiUrl)
				.withSubject(clientId)
				.sign(Algorithm.ECDSA256(new AppleEcdsaKeyProvider(privateKey, keyId)));
		} catch (Exception e) {
			throw new OAuthException(APPLE_AUTH_ERROR);
		}
	}

	private BodyInserters.FormInserter<String> createAuthRequestBody(String authorizationCode, String clientSecret) {
		return BodyInserters.fromFormData("client_id", clientId)
			.with("client_secret", clientSecret)
			.with("code", authorizationCode)
			.with("grant_type", "authorization_code");
	}

	private Mono<? extends Throwable> handleErrorResponse(ClientResponse response, ErrorMessage errorMessage) {
		return response.bodyToMono(String.class)
			.flatMap(body -> Mono.error(new OAuthException(errorMessage)));
	}

	private String fetchApplePublicKeys() {
		return webClient.get()
			.uri(appleApiUrl + "/auth/keys")
			.retrieve()
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

	private Member findMemberFromDB(String socialId, String socialType) {
		return memberRepository.findBySocialIdAndSocialTypeAndDeletedAtIsNull(socialId,
			SocialType.valueOf(socialType)).orElse(null);
	}
}
