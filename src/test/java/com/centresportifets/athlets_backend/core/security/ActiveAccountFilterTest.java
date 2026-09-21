package com.centresportifets.athlets_backend.core.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Optional;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import com.centresportifets.athlets_backend.user.*;

class ActiveAccountFilterTest {
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test
    void revokedSessionStaysInvalidAfterReactivation() throws Exception {
        UserAccount user = new UserAccount();
        user.setId(1L); user.setAccountStatus("Active"); user.setSessionVersion(1L);
        var users = mock(UserAccountRepository.class);
        when(users.findByUsername("coach")).thenReturn(Optional.of(user));
        var request = new MockHttpServletRequest();
        request.getSession().setAttribute("accountId", 1L);
        request.getSession().setAttribute("accountSessionVersion", 0L);
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("coach", null, List.of()));
        new ActiveAccountFilter(users).doFilter(request, response, chain);
        assertEquals(401, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(chain);
    }

    @Test
    void activeCurrentSessionContinues() throws Exception {
        UserAccount user = new UserAccount();
        user.setId(1L); user.setAccountStatus("Active");
        var users = mock(UserAccountRepository.class);
        when(users.findByUsername("coach")).thenReturn(Optional.of(user));
        var request = new MockHttpServletRequest();
        request.getSession().setAttribute("accountId", 1L);
        request.getSession().setAttribute("accountSessionVersion", 0L);
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("coach", null, List.of()));
        new ActiveAccountFilter(users).doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }
}
