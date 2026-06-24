package com.centresportifets.athlets_backend.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.centresportifets.athlets_backend.auth.dto.AuthCredentials;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthController authController;

    private AuthCredentials credentials;

    @BeforeEach
    void setUp() {
        credentials = new AuthCredentials("testUser", "testPassword");
    }

    @Test
    void loginUser_WithValidCredentials_ReturnsOk() {
        AuthUser mockUser = mock(AuthUser.class);
        
        when(authService.verifyAndFetchUser("testUser", "testPassword"))
                .thenReturn(Optional.of(mockUser));

        ResponseEntity<?> result = authController.loginUser(credentials, request, response);

        verify(authService).verifyAndFetchUser("testUser", "testPassword");
        verify(authService).loginUser(mockUser, request, response);
        
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void loginUser_WithInvalidCredentials_ReturnsUnauthorized() {
        when(authService.verifyAndFetchUser("testUser", "testPassword"))
                .thenReturn(Optional.empty());

        ResponseEntity<?> result = authController.loginUser(credentials, request, response);

		verify(authService).verifyAndFetchUser("testUser", "testPassword");
        verify(authService, never()).loginUser(any(), any(), any());
        
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(result.getBody()).isEqualTo("Invalid username or password");
    }

    @Test
    void logoutRoute() {
        Authentication authentication = mock(Authentication.class);

        ResponseEntity<?> result = authController.logoutUser(authentication, request, response);

        verify(authService).logoutUser(authentication, request, response);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}