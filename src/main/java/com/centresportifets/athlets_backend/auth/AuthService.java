package com.centresportifets.athlets_backend.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
	private final AuthUserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	private final SecurityContextRepository securityContextRepository =
			new HttpSessionSecurityContextRepository();

	public AuthService(AuthUserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * Verifies inbound login attempts.
	 *
	 * @param username Username to be verified
	 * @param rawPassword Unencrypted password
	 * @return the authenticated user object if an account is associated with the credentials
	 */
	public Optional<AuthUser> verifyAndFetchUser(String username, String rawPassword) {
		Optional<AuthUser> user = userRepository.findByName(username);

		if (user.isEmpty()) {
			return Optional.empty();
		}

		AuthUser realUser = user.get();
		return passwordEncoder.matches(rawPassword, realUser.getPassword())
				? Optional.of(realUser)
				: Optional.empty();
	}

	/**
	 * Logs in the user to springboot, and creates the JSESSIONID token that is sent to the frontend
	 * browser
	 *
	 * @param authuser authenticated user that has been fetched with the appropriate credentials
	 * @param request the incoming HTTP request used to bind and establish the security context
	 *     session
	 * @param response the outgoing HTTP response where the JSESSIONID cookie is injected upon
	 *     success
	 */
	public void loginUser(
			AuthUser authuser, HttpServletRequest request, HttpServletResponse response) {
		Authentication authentication =
				UsernamePasswordAuthenticationToken.authenticated(
						authuser.getName(), null, List.of(new SimpleGrantedAuthority("ADMIN")));

		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);

		SecurityContextHolder.setContext(context);

		securityContextRepository.saveContext(context, request, response);
	}
}
