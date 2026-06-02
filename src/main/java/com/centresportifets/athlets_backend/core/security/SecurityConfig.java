package com.centresportifets.athlets_backend.core.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@Configuration
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityContextLogoutHandler logoutHandler(){
		return new SecurityContextLogoutHandler();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(
				auth ->
					auth.requestMatchers("/api/auth/login")
						.permitAll()
						.requestMatchers(
							"/v3/api-docs/**",
							"/swagger-ui/**",
							"/swagger-ui.html")
						.permitAll()
						.anyRequest()
						.authenticated());

		return http.build();
	}
}
