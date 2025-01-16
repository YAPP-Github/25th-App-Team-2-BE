package com.tnt.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

	@Bean
	public WebClient webClient() {
		return WebClient.builder()
			.codecs(configurer -> configurer
				.defaultCodecs()
				.maxInMemorySize(16 * 1024 * 1024))  // 최대 16MB
			.build();
	}
}
