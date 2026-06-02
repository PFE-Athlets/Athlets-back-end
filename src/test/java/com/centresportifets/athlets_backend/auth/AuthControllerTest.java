package com.centresportifets.athlets_backend.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

class AuthControllerTest {

	@Test
	void logoutRoute() {
		AuthService authService = mock(AuthService.class);
		AuthController authController = new AuthController(authService);
		Authentication authentication = mock(Authentication.class);
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		ResponseEntity<?> result = authController.logoutUser(authentication, request, response);

		verify(authService).logoutUser(authentication, request, response);
		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
}