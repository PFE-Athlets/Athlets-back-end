package com.centresportifets.athlets_backend.user.intervenant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.centresportifets.athlets_backend.auth.AuthService;
import com.centresportifets.athlets_backend.user.*;
import com.centresportifets.athlets_backend.user.coach.Coach;
import com.centresportifets.athlets_backend.user.kine.KineTeamRepository;
import com.centresportifets.athlets_backend.user.intervenant.dto.*;

@ExtendWith(MockitoExtension.class)
class IntervenantServiceTest {
    @Mock UserAccountRepository users;
    @Mock KineTeamRepository kineTeams;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthService authService;
    @InjectMocks IntervenantService service;

    @Test
    void createsPendingCoachWithNoTeamAndSendsInvitation() {
        when(passwordEncoder.encode(anyString())).thenReturn("random-password-hash");
        doAnswer(invocation -> { ((UserAccount) invocation.getArgument(0)).setId(12L); return invocation.getArgument(0); })
                .when(users).saveAndFlush(any());
        var data = service.create(new IntervenantCreateRequest(" Alice ", "Martin", "alice@example.com", null, "alice", UserType.COACH));
        assertEquals("Alice", data.firstName());
        assertEquals("Pending", data.accountStatus());
        assertEquals(UserType.COACH, data.role());
        assertFalse(data.accountActivated());
        assertTrue(data.teamIds().isEmpty());
        verify(authService).generateActivationTokenForUser(argThat(user -> user instanceof Coach
                && user.getPassword().equals("random-password-hash") && !user.isAccountActivated()));
    }

    @Test
    void rejectsAthleteRoleAndDuplicateEmail() {
        assertThrows(IllegalArgumentException.class, () -> service.create(
                new IntervenantCreateRequest("A", "B", "a@b.ca", null, "ab", UserType.ATHLETE)));
        when(users.existsByEmail("a@b.ca")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.create(
                new IntervenantCreateRequest("A", "B", "a@b.ca", null, "ab", UserType.ADMIN)));
        verify(users, never()).saveAndFlush(any());
        verifyNoInteractions(authService);
    }

    @Test
    void reactivationPreservesPasswordAndTeamOfVerifiedAccount() {
        Coach coach = coach(false);
        coach.setAccountActivated(true);
        var team = new com.centresportifets.athlets_backend.team.Team();
        team.setId(4L);
        coach.setTeam(team);
        var data = service.reactivate(12L);
        assertEquals("Active", data.accountStatus());
        assertEquals(java.util.List.of(4L), data.teamIds());
        assertEquals("existing-hash", coach.getPassword());
        verifyNoInteractions(passwordEncoder, authService);
    }

    @Test
    void reactivationOfUnverifiedAccountRequiresEmail() {
        Coach coach = coach(false);
        assertEquals("Pending", service.reactivate(12L).accountStatus());
        verify(authService).generateActivationTokenForUser(coach);
        assertFalse(coach.isAccountActivated());
    }

    @Test
    void changingPendingEmailInvalidatesOldLinkAndSendsNewInvitation() {
        Coach coach = coach(true);
        service.update(12L, new IntervenantUpdateRequest("Alice", "Martin", "new@example.com", "123", "alice"));
        var order = inOrder(authService);
        order.verify(authService).invalidateAccountTokens(coach);
        order.verify(authService).generateActivationTokenForUser(coach);
        assertEquals("new@example.com", coach.getEmail());
    }

    private Coach coach(boolean pending) {
        Coach coach = new Coach();
        coach.setId(12L);
        coach.setEmail("old@example.com");
        coach.setPassword("existing-hash");
        coach.setAccountStatus(pending ? "Pending" : "Inactive");
        when(users.findById(12L)).thenReturn(Optional.of(coach));
        return coach;
    }
}
