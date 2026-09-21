package com.centresportifets.athlets_backend.user.intervenant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import com.centresportifets.athlets_backend.auth.AuthService;
import com.centresportifets.athlets_backend.user.UserAccountRepository;
import com.centresportifets.athlets_backend.user.kine.KineTeamRepository;

@SpringJUnitConfig(IntervenantSecurityTest.Config.class)
class IntervenantSecurityTest {
    @Configuration
    @EnableMethodSecurity
    static class Config {
        @Bean AuthService authService() { return mock(AuthService.class); }
        @Bean UserAccountRepository users() { return mock(UserAccountRepository.class); }
        @Bean IntervenantService intervenantService(UserAccountRepository users, AuthService auth) {
            return new IntervenantService(users, mock(KineTeamRepository.class), mock(PasswordEncoder.class), auth);
        }
    }
    @Autowired IntervenantService service;
    @Autowired AuthService auth;
    @Autowired UserAccountRepository users;
    @AfterEach void clear() { SecurityContextHolder.clearContext(); reset(auth, users); }

    @Test
    void nonAdminCannotUseAnyStaffManagementOperation() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("coach", null, List.of()));
        assertThrows(AccessDeniedException.class, () -> service.list());
        assertThrows(AccessDeniedException.class, () -> service.get(1L));
        assertThrows(AccessDeniedException.class, () -> service.create(null));
        assertThrows(AccessDeniedException.class, () -> service.update(1L, null));
        assertThrows(AccessDeniedException.class, () -> service.deactivate(1L, null));
        assertThrows(AccessDeniedException.class, () -> service.reactivate(1L));
        assertThrows(AccessDeniedException.class, () -> service.resendActivation(1L));
        verifyNoInteractions(users);
    }

    @Test
    void adminCanListStaff() {
        var authentication = new UsernamePasswordAuthenticationToken("admin", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(auth.hasPermission(authentication, "ADMIN")).thenReturn(true);
        assertTrue(service.list().isEmpty());
        verify(users).findByAccessLevelInOrderByLastNameAscFirstNameAsc(List.of(1, 2, 4));
    }
}
