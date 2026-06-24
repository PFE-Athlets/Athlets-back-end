package com.centresportifets.athlets_backend.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityContextLogoutHandler logoutHandler;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void verifyAndFetchUser_WhenUserDoesNotExist_ReturnsEmpty() {
        String username = "missingUser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        Optional<AuthUser> result = authService.verifyAndFetchUser(username, "anyPassword");

        assertTrue(result.isEmpty());
        verify(userRepository).findByUsername(username);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void verifyAndFetchUser_WhenPasswordDoesNotMatch_ReturnsEmpty() {
        String username = "existingUser";
        String rawPassword = "wrongPassword";
        AuthUser mockUser = mock(AuthUser.class);
        when(mockUser.getPassword()).thenReturn("encodedPassword");
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(rawPassword, "encodedPassword")).thenReturn(false);

        Optional<AuthUser> result = authService.verifyAndFetchUser(username, rawPassword);

        assertTrue(result.isEmpty());
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder).matches(rawPassword, "encodedPassword");
    }

    @Test
    void verifyAndFetchUser_WhenCredentialsAreValid_ReturnsUser() {
        String username = "validUser";
        String rawPassword = "correctPassword";
        AuthUser mockUser = mock(AuthUser.class);
        when(mockUser.getPassword()).thenReturn("encodedPassword");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(rawPassword, "encodedPassword")).thenReturn(true);

        Optional<AuthUser> result = authService.verifyAndFetchUser(username, rawPassword);

        assertTrue(result.isPresent());
        assertEquals(mockUser, result.get());
    }

    @Test
    void loginUser_SetsSecurityContextAndSavesIt() {
        AuthUser mockUser = mock(AuthUser.class);
        when(mockUser.getUsername()).thenReturn("john_doe");
        
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        authService.loginUser(mockUser, request, response);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("john_doe", auth.getName());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN")));
    }

    @Test
    void logoutLogic() {
        Authentication authentication = mock(Authentication.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        authService.logoutUser(authentication, request, response);

        verify(logoutHandler).logout(request, response, authentication);
    }
}