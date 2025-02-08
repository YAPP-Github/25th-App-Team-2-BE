package com.tnt.gateway.config;

import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tnt.common.error.model.ErrorResponse;
import com.tnt.gateway.filter.SessionAuthenticationFilter;
import com.tnt.gateway.service.SessionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private static final String[] ALLOWED_URIS = {
		"/",
		"/oauth2/**",
		"/login/**",
		"/api",
		"/v3/api-docs/**",
		"/swagger-ui/**",
		"/members/sign-up"
	};

	private final ObjectMapper objectMapper;
	private final SessionService sessionService;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.cors(Customizer.withDefaults())
			.formLogin(AbstractHttpConfigurer::disable)
			.logout(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.csrf(AbstractHttpConfigurer::disable)
			.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
			.sessionManagement(sessionManagement ->
				sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(request -> request
				.requestMatchers(ALLOWED_URIS).permitAll().anyRequest().authenticated())
			.addFilterAfter(sessionAuthenticationFilter(), LogoutFilter.class)
			.exceptionHandling(e -> e.authenticationEntryPoint((request, response, authException) -> {
				log.error("(Invalid URL) Security Filter Error: {}", authException.getMessage(), authException);

				response.setStatus(SC_NOT_FOUND);
				response.setContentType("application/json;charset=UTF-8");
				response.getWriter()
					.write(objectMapper.writeValueAsString(new ErrorResponse("Invalid URL")));
			}));

		return http.build();
	}

	@Bean
	public SessionAuthenticationFilter sessionAuthenticationFilter() {
		return new SessionAuthenticationFilter(Arrays.asList(ALLOWED_URIS), sessionService, objectMapper);
	}
}
