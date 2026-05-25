package com.centresportifets.athlets_backend.auth;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Tag(
	name = "Authentication controller",
	description = "Handles basic user authentication flow and account creation")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	private record AuthCredentials(String username, String password) {}

	/**
	 * Handles the entire login flow of the application
	 *
	 * @param credentials is the body of the post request, consisting of a username and unencrypted
	 *     password
	 * @param request the incoming HTTP request used to bind and establish the security context
	 *     session
	 * @param response the outgoing HTTP response where the JSESSIONID cookie is injected upon
	 *     success
	 * @return a {@link ResponseEntity} returning {@code 200 OK} with temporary placeholder text on
	 *     success, or {@code 401 Unauthorized} if authentication fails. TO CHANGE - returns nothing
	 *     for now, once the database defined, a DTO with the appropriate user data
	 */
	@PostMapping("/login")
	public ResponseEntity<?> loginUser(
			@RequestBody AuthCredentials credentials,
			HttpServletRequest request,
			HttpServletResponse response) {
		Optional<AuthUser> authUserOpt =
				authService.verifyAndFetchUser(credentials.username, credentials.password);

		if (authUserOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body("Invalid username or password");
		}

		authService.loginUser(authUserOpt.get(), request, response);

		return ResponseEntity.ok("works");
	}
}
