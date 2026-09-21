package com.centresportifets.athlets_backend.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.centresportifets.athlets_backend.auth.dto.ActivateAccountRequest;
import com.centresportifets.athlets_backend.auth.token.*;
import com.centresportifets.athlets_backend.email.EmailService;
import com.centresportifets.athlets_backend.team.*;
import com.centresportifets.athlets_backend.user.*;
import com.centresportifets.athlets_backend.user.athlete.*;
import com.centresportifets.athlets_backend.user.coach.*;
import com.centresportifets.athlets_backend.user.kine.*;

@ExtendWith(MockitoExtension.class)
class AccountLifecycleTest {
    @Mock UserAccountRepository users;
    @Mock AccountTokenRepository tokens;
    @Mock PasswordEncoder passwords;
    @Mock EmailService email;
    @Mock CoachRepository coaches;
    @Mock KineRepository kines;
    @Mock KineTeamRepository kineTeams;
    @Mock AthleteRepository athletes;
    @InjectMocks AuthService service;

    @Test
    void pendingAndInactiveAccountsCannotLoginOrUseExistingPermissions() {
        for (String status : List.of("Pending", new String("Inactive"))) {
            UserAccount user = user(1L, 1, status);
            when(users.findByUsername("user")).thenReturn(Optional.of(user));
            assertTrue(service.verifyAndFetchUser("user", "password").isEmpty());
            assertFalse(service.hasPermission(auth(), "ADMIN"));
            assertEquals(UserType.INVALID, service.getAuthenticatedUserType(auth()));
        }
        verifyNoInteractions(passwords);
    }

    @Test
    void unassignedCoachCannotAccessAnyTeamOrAthlete() {
        when(users.findByUsername("user")).thenReturn(Optional.of(user(1L, 2, "Active")));
        when(coaches.findByUsername("user")).thenReturn(Optional.of(new Coach()));
        assertFalse(service.canAccessTeams(auth(), List.of(10L)));
        assertTrue(service.accessibleTeamIds(auth()).isEmpty());
        assertFalse(service.canManageAthletes(auth(), List.of("missing")));
    }

    @Test
    void kineCannotManageMissingAthletes() {
        when(users.findByUsername("user")).thenReturn(Optional.of(user(1L, 4, "Active")));
        when(kines.findByUsername("user")).thenReturn(Optional.of(new Kine()));
        assertFalse(service.canManageAthletes(auth(), List.of("missing")));
    }

    @Test
    void administratorCannotDeactivateSelfOrLastActiveAdmin() {
        UserAccount admin = user(1L, 1, "Active");
        when(users.findByUsername("user")).thenReturn(Optional.of(admin));
        when(users.findByAccessLevelOrderByIdAsc(1)).thenReturn(List.of(admin));
        when(users.findById(1L)).thenReturn(Optional.of(admin));
        assertThrows(AccessDeniedException.class, () -> service.setUserInactive(1L, auth()));
        UserAccount other = user(2L, 1, "Active");
        when(users.findById(2L)).thenReturn(Optional.of(other));
        when(users.findByAccessLevelOrderByIdAsc(1)).thenReturn(List.of(other));
        assertThrows(AccessDeniedException.class, () -> service.setUserInactive(2L, auth()));
        verify(users, never()).save(any());
    }

    @Test
    void deactivationPreservesActivationHistoryAndInvalidatesTokens() {
        UserAccount admin = user(1L, 1, "Active");
        UserAccount coach = user(2L, 2, "Active");
        coach.setAccountActivated(true);
        when(users.findByUsername("user")).thenReturn(Optional.of(admin));
        when(users.findById(2L)).thenReturn(Optional.of(coach));
        AccountToken token = new AccountToken();
        when(tokens.findByUserAndTypeAndUsedAtIsNull(coach, "ACTIVATION")).thenReturn(List.of());
        when(tokens.findByUserAndTypeAndUsedAtIsNull(coach, "PASSWORD_RESET")).thenReturn(List.of(token));
        service.setUserInactive(2L, auth());
        assertEquals("Inactive", coach.getAccountStatus());
        assertTrue(coach.isAccountActivated());
        assertTrue(token.isUsed());
    }

    @Test
    void activationSetsPasswordAndHistoryAndCannotReuseLink() {
        UserAccount coach = user(2L, 2, "Pending");
        AccountToken token = new AccountToken();
        token.setType("ACTIVATION"); token.setUser(coach);
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        when(tokens.findByToken("link")).thenReturn(Optional.of(token));
        when(passwords.encode("new-password")).thenReturn("hash");
        ActivateAccountRequest request = new ActivateAccountRequest();
        request.setToken("link"); request.setNewPassword("new-password"); request.setConfirmPassword("new-password");
        service.activateAccount(request);
        assertEquals("Active", coach.getAccountStatus());
        assertEquals("hash", coach.getPassword());
        assertTrue(coach.isAccountActivated());
        assertThrows(IllegalArgumentException.class, () -> service.activateAccount(request));
    }

    @Test
    void resendingInvitationInvalidatesPreviousLink() {
        UserAccount coach = user(2L, 2, "Pending");
        coach.setEmail("coach@example.com");
        AccountToken old = new AccountToken();
        when(tokens.findByUserAndTypeAndUsedAtIsNull(coach, "ACTIVATION")).thenReturn(List.of(old));
        service.generateActivationTokenForUser(coach);
        assertTrue(old.isUsed());
        verify(email).sendActivationEmail(eq("coach@example.com"), contains("activation-compte?token="));
        verify(tokens).save(argThat(token -> !token.isUsed() && token.getExpiresAt().isAfter(LocalDateTime.now().plusHours(71))));
    }

    private Authentication auth() { return new UsernamePasswordAuthenticationToken("user", null, List.of()); }
    private UserAccount user(Long id, int role, String status) {
        UserAccount user = new UserAccount(); user.setId(id); user.setAccessLevel(role); user.setAccountStatus(status);
        return user;
    }
}
