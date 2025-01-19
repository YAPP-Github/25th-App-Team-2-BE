package com.tnt.global.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@Configuration
@Profile({"prod", "dev"})
public class FcmConfig {

	@Value("${firebase.config.path}")
	private String firebaseConfigPath;

	@Bean
	public FirebaseApp firebaseApp() throws IOException {
		Resource serviceAccount = new ClassPathResource(firebaseConfigPath);

		FirebaseOptions options = FirebaseOptions.builder()
			.setCredentials(GoogleCredentials.fromStream(serviceAccount.getInputStream()))
			.build();

		return FirebaseApp.initializeApp(options);
	}
}
