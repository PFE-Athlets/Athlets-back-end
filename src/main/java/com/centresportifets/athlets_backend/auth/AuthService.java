package com.centresportifets.athlets_backend.auth;

import com.centresportifets.athlets_backend.auth.dto.ActivateAccountRequest;
import com.centresportifets.athlets_backend.auth.token.AccountToken;
import com.centresportifets.athlets_backend.auth.token.AccountTokenRepository;
import com.centresportifets.athlets_backend.user.UserAccount;
import com.centresportifets.athlets_backend.user.UserAccountRepository;
import com.centresportifets.athlets_backend.user.UserType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import com.centresportifets.athlets_backend.email.EmailService;
import org.springframework.beans.factory.annotation.Value;

@RequiredArgsConstructor
@Component("authService")
@Service
public class AuthService {

	private static final String ACTIVATION_TOKEN_TYPE = "ACTIVATION";
	private static final String ACCOUNT_STATUS_TO_ACTIVATE = "A_ACTIVER";
	private static final String ACCOUNT_STATUS_ACTIVE = "Active";
	private static final int ACTIVATION_TOKEN_EXPIRATION_HOURS = 24;

	private final UserAccountRepository userRepository;
	private final AccountTokenRepository accountTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final SecurityContextLogoutHandler logoutHandler;
	private final SecurityContextRepository securityContextRepository =
			new HttpSessionSecurityContextRepository();

	private final EmailService emailService;

	@Value("${app.frontend.base-url:http://localhost:5173}")
	private String frontendBaseUrl;

	/**
	 * Verifies inbound login attempts.
	 *
	 * @param username Username to be verified
	 * @param rawPassword Unencrypted password
	 * @return the authenticated user object if an account is associated with the credentials
	 */
	public Optional<UserAccount> verifyAndFetchUser(String username, String rawPassword) {
		Optional<UserAccount> user = userRepository.findByUsername(username);

		if (user.isEmpty()) {
			return Optional.empty();
		}

		UserAccount realUser = user.get();

		if (!ACCOUNT_STATUS_ACTIVE.equals(realUser.getAccountStatus())) {
			return Optional.empty();
		}

		return passwordEncoder.matches(rawPassword, realUser.getPassword())
				? Optional.of(realUser)
				: Optional.empty();
	}

	/**
	 * Generates an activation token for an existing user.
	 * This method is temporary until athlete creation automatically triggers token creation.
	 *
	 * @param username Username of the account to activate
	 * @return Activation link to use from the frontend
	 */
	public String generateActivationTokenForUsername(String username) {
		if (username == null || username.isBlank()) {
			throw new IllegalArgumentException("Le nom d'utilisateur est obligatoire.");
		}

		UserAccount user = userRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("Aucun utilisateur trouvé avec ce nom d'utilisateur."));

		return generateActivationTokenForUser(user);
	}

	/**
	 * Generates an activation token for a user account.
	 *
	 * @param user User account to activate
	 * @return Activation link to use from the frontend
	 */
	public String generateActivationTokenForUser(UserAccount user) {
		if (user == null) {
			throw new IllegalArgumentException("L'utilisateur est obligatoire.");
		}

		if (!ACCOUNT_STATUS_TO_ACTIVATE.equals(user.getAccountStatus())) {
			throw new IllegalArgumentException("Ce compte n'est pas en attente d'activation.");
		}

		List<AccountToken> activeTokens =
				accountTokenRepository.findByUserAndTypeAndUsedAtIsNull(user, ACTIVATION_TOKEN_TYPE);

		for (AccountToken activeToken : activeTokens) {
			activeToken.setUsedAt(LocalDateTime.now());
		}

		accountTokenRepository.saveAll(activeTokens);

		String tokenValue = UUID.randomUUID().toString();

		AccountToken accountToken = new AccountToken();
		accountToken.setToken(tokenValue);
		accountToken.setType(ACTIVATION_TOKEN_TYPE);
		accountToken.setUser(user);
		accountToken.setExpiresAt(LocalDateTime.now().plusHours(ACTIVATION_TOKEN_EXPIRATION_HOURS));

		accountTokenRepository.save(accountToken);

		String activationLink = frontendBaseUrl + "/activation-compte?token=" + tokenValue;

		// TODO: Uncomment when email sending is enabled.
		// emailService.sendActivationEmail(user.getEmail(), activationLink);

		System.out.println("Lien d'activation généré : " + activationLink);

		return activationLink;
	}

	/**
	 * Activates an account using a valid activation token and sets the user's password.
	 *
	 * @param request Activation request containing token and new password
	 */
	public void activateAccount(ActivateAccountRequest request) {
		validateActivationRequest(request);

		AccountToken accountToken = accountTokenRepository.findByToken(request.getToken())
				.orElseThrow(() -> new IllegalArgumentException("Le lien d'activation est invalide ou expiré."));

		if (!ACTIVATION_TOKEN_TYPE.equals(accountToken.getType())) {
			throw new IllegalArgumentException("Le lien d'activation est invalide.");
		}

		if (accountToken.isUsed()) {
			throw new IllegalArgumentException("Ce lien d'activation a déjà été utilisé.");
		}

		if (accountToken.isExpired()) {
			throw new IllegalArgumentException("Ce lien d'activation est expiré.");
		}

		UserAccount user = accountToken.getUser();

		if (!ACCOUNT_STATUS_TO_ACTIVATE.equals(user.getAccountStatus())) {
			throw new IllegalArgumentException("Ce compte est déjà activé ou ne peut pas être activé.");
		}

		user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		user.setAccountStatus(ACCOUNT_STATUS_ACTIVE);

		accountToken.setUsedAt(LocalDateTime.now());

		userRepository.save(user);
		accountTokenRepository.save(accountToken);
	}

	private void validateActivationRequest(ActivateAccountRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("La demande d'activation est invalide.");
		}

		if (request.getToken() == null || request.getToken().isBlank()) {
			throw new IllegalArgumentException("Le lien d'activation est invalide.");
		}

		if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
			throw new IllegalArgumentException("Le nouveau mot de passe est obligatoire.");
		}

		if (request.getConfirmPassword() == null || request.getConfirmPassword().isBlank()) {
			throw new IllegalArgumentException("La confirmation du mot de passe est obligatoire.");
		}

		if (!request.getNewPassword().equals(request.getConfirmPassword())) {
			throw new IllegalArgumentException("Les mots de passe ne correspondent pas.");
		}

		if (request.getNewPassword().length() < 8) {
			throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères.");
		}
	}

	/**
	 * Logs in the user to springboot, and creates the JSESSIONID token that is sent to the frontend
	 * browser
	 *
	 * @param UserAccount authenticated user that has been fetched with the appropriate credentials
	 * @param request the incoming HTTP request used to bind and establish the security context
	 *     session
	 * @param response the outgoing HTTP response where the JSESSIONID cookie is injected upon
	 *     success
	 */
	public void loginUser(
			UserAccount UserAccount, HttpServletRequest request, HttpServletResponse response) {
		Authentication authentication =
				UsernamePasswordAuthenticationToken.authenticated(
						UserAccount.getUsername(), null, List.of(new SimpleGrantedAuthority("ADMIN")));

		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);

		SecurityContextHolder.setContext(context);

		securityContextRepository.saveContext(context, request, response);
	}

	/**
	 * Logs out the user from springboot, and invalidates the JSESSIONID token on the frontend
	 * browser
	 *
	 * @param authentication the current authentication object of the user to be logged out, used to
	 *     invalidate the security context session
	 * @param request the incoming HTTP request used to bind and establish the security context
	 *     session
	 * @param response the outgoing HTTP response where the JSESSIONID cookie is injected upon
	 *     success
	 */
	public void logoutUser(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
		logoutHandler.logout(request, response, authentication);
	}

	/**
	 *  Checks if the userId provided (like the one for completing a test) corresponds to the user connected to the backend
	 */
	public boolean checkIfUserIsAuthenticatedUser(Long userId, Authentication auth) {
		Optional<UserAccount> userOpt = userRepository.findByUsername(auth.getName());
		if (userOpt.isEmpty()) {
			return false;
		}
		UserAccount authenticatedUser = userOpt.get();
		return authenticatedUser.getId().equals(userId);
	}

	public boolean checkIfUserIsAuthenticatedUser(UserAccount user, Authentication auth) {
		return checkIfUserIsAuthenticatedUser(user.getId(), auth);
	}

	/**
	 *  Verify that the authenticated user has the appropriate permission for an access 
	 */
	public boolean hasPermission(Authentication auth, String userTypeName) {
		UserType userType = UserType.valueOf(userTypeName);
		Optional<UserAccount> userOpt = userRepository.findByUsername(auth.getName());
		if (userOpt.isEmpty()) {
			return false;
		}
		UserAccount authenticatedUser = userOpt.get();
		return userType.getPermissionLevel() == (authenticatedUser.getAccessLevel());
	}

	/**
	 *  Retrieves the usertype of the current authenticated user
	 */
	public UserType getAuthenticatedUserType(Authentication auth){
		int permissionLevel = userRepository.findByUsername(auth.getName()).orElseThrow(() -> new IllegalArgumentException("No user logged in the backend")).getAccessLevel();
		switch (permissionLevel){
			case 1: return UserType.ADMIN;
			case 2: return UserType.COACH;
			case 3: return UserType.ATHLETE;
			default: return UserType.INVALID;
		}
	}
}