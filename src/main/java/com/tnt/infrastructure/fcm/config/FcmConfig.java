package com.tnt.infrastructure.fcm.config;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@Configuration
public class FcmConfig {

	@Value("${firebase.config.path}")
	private String firebaseConfigPath;

	@Bean
	public FirebaseApp firebaseApp(Environment environment) throws IOException {
		Resource serviceAccount;

		if (Arrays.asList(environment.getActiveProfiles()).contains("local")) {
			serviceAccount = new FileSystemResource(firebaseConfigPath);
		} else {
			serviceAccount = new ClassPathResource(firebaseConfigPath);
		}

		FirebaseOptions options = FirebaseOptions.builder()
			.setCredentials(GoogleCredentials.fromStream(serviceAccount.getInputStream()))
			.build();

		if (FirebaseApp.getApps().isEmpty()) {
			return FirebaseApp.initializeApp(options);
		}

		return FirebaseApp.getInstance();
	}
}
