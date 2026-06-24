package com.centresportifets.athlets_backend.auth;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private AuthRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private SecurityContextLogoutHandler logoutHandler;

	@Test
	void logoutLogic() {
		AuthService authService = new AuthService(userRepository, passwordEncoder, logoutHandler);
		Authentication authentication = mock(Authentication.class);
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		authService.logoutUser(authentication, request, response);

		verify(logoutHandler).logout(request, response, authentication);
	}
}
