package com.tnt.global.config;

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

import com.tnt.application.auth.SessionService;
import com.tnt.global.auth.filter.ServletExceptionFilter;
import com.tnt.global.auth.filter.SessionAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private static final String[] ALLOWED_URIS = {
		"/",
		"/index.html",
		"/api",
		"/v3/api-docs/**",
		"/swagger-ui/**",
		"/login/**",
		"/member/sign-up"
	};
	private final SessionService sessionService;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.cors(Customizer.withDefaults())
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.csrf(AbstractHttpConfigurer::disable)
			.headers(headers ->
				headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
			.sessionManagement(sessionManagement ->
				sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)
			.authorizeHttpRequests(request ->
				request.requestMatchers(ALLOWED_URIS).permitAll()
					.anyRequest().authenticated())
			.addFilterBefore(servletExceptionFilter(), LogoutFilter.class)
			.addFilterAfter(sessionAuthenticationFilter(), LogoutFilter.class);

		return http.build();
	}

	@Bean
	public ServletExceptionFilter servletExceptionFilter() {
		return new ServletExceptionFilter();
	}

	@Bean
	public SessionAuthenticationFilter sessionAuthenticationFilter() {
		return new SessionAuthenticationFilter(Arrays.asList(ALLOWED_URIS), sessionService);
	}
}
