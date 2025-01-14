package com.tnt.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
@Profile("dev")
@OpenAPIDefinition(servers = {@Server(url = "http://dev-api.tntapp.co.kr")})
public class SwaggerConfig {

	@Bean
	public OpenAPI openApi() {
		return new OpenAPI()
			.components(new Components())
			.info(apiInfo());
	}

	private Info apiInfo() {
		Contact contact = new Contact();
		contact.setEmail("ymkim97@gmail.com");

		return new Info()
			.title("TnT API")
			.description("Swagger UI for TnT API")
			.version("1.0.0")
			.contact(contact);
	}
}
