package com.tnt.application.member;

import static com.tnt.global.error.model.ErrorMessage.*;

import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import com.auth0.jwt.interfaces.ECDSAKeyProvider;
import com.tnt.global.error.exception.OAuthException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AppleEcdsaKeyProvider implements ECDSAKeyProvider {

	private final String privateKey;
	private final String keyId;

	@Override
	public ECPublicKey getPublicKeyById(String keyId) {
		return null;  // 클라이언트 시크릿 생성에는 불필요
	}

	@Override
	public ECPrivateKey getPrivateKey() {
		try {
			byte[] pkcs8EncodedBytes = Base64.getDecoder().decode(privateKey);
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedBytes);
			KeyFactory kf = KeyFactory.getInstance("EC");
			return (ECPrivateKey)kf.generatePrivate(keySpec);
		} catch (Exception e) {
			throw new OAuthException(FAILED_TO_FETCH_PRIVATE_KEY);
		}
	}

	@Override
	public String getPrivateKeyId() {
		return keyId;
	}
}
