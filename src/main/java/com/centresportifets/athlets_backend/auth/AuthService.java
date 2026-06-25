package com.centresportifets.athlets_backend.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
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

import com.centresportifets.athlets_backend.auth.userTypes.UserType;

@RequiredArgsConstructor
@Component("authService")
@Service
public class AuthService {
	private final AuthRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final SecurityContextLogoutHandler logoutHandler;
	private final SecurityContextRepository securityContextRepository =
			new HttpSessionSecurityContextRepository();

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
		return passwordEncoder.matches(rawPassword, realUser.getPassword())
				? Optional.of(realUser)
				: Optional.empty();
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

	public boolean checkPermission(Authentication auth, String userTypeName) {
		UserType userType = UserType.valueOf(userTypeName);
		Optional<UserAccount> userOpt = userRepository.findByUsername(auth.getName());
		if (userOpt.isEmpty()) {
			return false;
		}
		UserAccount authenticatedUser = userOpt.get();
		return userType.getPermissionLevel() == (authenticatedUser.getAccessLevel());
	}

	public UserType getAuthenticatedUserType(Authentication auth){
		int permissionLevel = userRepository.findByUsername(auth.getName()).orElseThrow(() -> new IllegalArgumentException("User not found")).getAccessLevel();
		switch (permissionLevel){
			case 3: return UserType.ADMIN;
			case 2: return UserType.COACH;
			case 1: 
			default: return UserType.ATHLETE;
		}
	}
}
