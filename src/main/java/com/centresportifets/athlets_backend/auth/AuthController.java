package com.centresportifets.athlets_backend.auth;

import com.centresportifets.athlets_backend.auth.dto.ActivateAccountRequest;
import com.centresportifets.athlets_backend.auth.dto.AuthCredentials;
import com.centresportifets.athlets_backend.auth.dto.GenerateActivationTokenRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.centresportifets.athlets_backend.auth.dto.AuthCredentials;
import com.centresportifets.athlets_backend.auth.dto.AuthUser;
import com.centresportifets.athlets_backend.user.UserAccount;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Tag(name = "Authentication controller", description = "Handles basic user authentication flow and account creation")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	/**
	 * Handles the entire login flow of the application
	 *
	 * @param credentials is the body of the post request, consisting of a username
	 *                    and unencrypted
	 *                    password
	 * @param request     the incoming HTTP request used to bind and establish the
	 *                    security context
	 *                    session
	 * @param response    the outgoing HTTP response where the JSESSIONID cookie is
	 *                    injected upon
	 *                    success
	 * @return a {@link ResponseEntity} returning {@code 200 OK} with temporary
	 *         placeholder text on
	 *         success, or {@code 401 Unauthorized} if authentication fails. TO
	 *         CHANGE - returns nothing
	 *         for now, once the database defined, a DTO with the appropriate user
	 *         data
	 */
	@PostMapping("/login")
	public ResponseEntity<?> loginUser(
			@RequestBody AuthCredentials credentials,
			HttpServletRequest request,
			HttpServletResponse response) {
		Optional<UserAccount> userAccountOpt = authService.verifyAndFetchUser(credentials.getUsername(), credentials.getPassword());

		if (userAccountOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
		}

		authService.loginUser(userAccountOpt.get(), request, response);

		return ResponseEntity.ok(new AuthUser(userAccountOpt.get()));
	}

	/**
	 * Temporary endpoint used to generate an activation token for an existing account.
	 * This should later be replaced by automatic token generation during athlete creation.
	 *
	 * @param request Request containing the username of the account to activate
	 * @return generated activation link
	 */
	@PostMapping("/dev/generate-activation-token")
	public ResponseEntity<?> generateActivationToken(
			@RequestBody GenerateActivationTokenRequest request) {
		try {
			String activationLink = authService.generateActivationTokenForUsername(request.getUsername());

			return ResponseEntity.ok(Map.of(
					"message", "Lien d'activation généré avec succès.",
					"activationLink", activationLink
			));
		} catch (IllegalArgumentException exception) {
			return ResponseEntity.badRequest().body(Map.of("erreur", exception.getMessage()));
		}
	}

	/**
	 * Activates an account using a valid activation token.
	 *
	 * @param request Request containing the activation token and new password
	 * @return success or error response
	 */
	@PostMapping("/activate")
	public ResponseEntity<?> activateAccount(@RequestBody ActivateAccountRequest request) {
		try {
			authService.activateAccount(request);

			return ResponseEntity.ok(Map.of("message", "Compte activé avec succès."));
		} catch (IllegalArgumentException exception) {
			return ResponseEntity.badRequest().body(Map.of("erreur", exception.getMessage()));
		}
	}

	/**
	 * Handles the logout flow of the application.
	 *
	 * @param authentication the current authentication object
	 * @param request the incoming HTTP request
	 * @param response the outgoing HTTP response
	 * @return a {@link ResponseEntity} returning {@code 200 OK}
	 */
	@PostMapping("/logout")
	public ResponseEntity<?> logoutUser(
			Authentication authentication,
			HttpServletRequest request,
			HttpServletResponse response) {

		authService.logoutUser(authentication, request, response);
		return ResponseEntity.ok().build();
	}
}
