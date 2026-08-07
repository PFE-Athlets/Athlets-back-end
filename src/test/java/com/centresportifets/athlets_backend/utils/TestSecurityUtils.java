package com.centresportifets.athlets_backend.utils;

import com.centresportifets.athlets_backend.auth.AuthService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class TestSecurityUtils {

    /**
     * Mocks both Spring Security context AND the authService.hasPermission response.
     */
    public static void mockUserSession(AuthService authServiceMock, String username, String role) {
        Authentication auth = new UsernamePasswordAuthenticationToken(username, null, java.util.List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        when(authServiceMock.hasPermission(any(), eq(role))).thenReturn(true);
    }

    public static void mockUnauthorizedSession(AuthService authServiceMock, String username) {
        Authentication auth = new UsernamePasswordAuthenticationToken(username, null, java.util.List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        when(authServiceMock.hasPermission(any(), any())).thenReturn(false);
    }

    public static void clearSession() {
        SecurityContextHolder.clearContext();
    }
}