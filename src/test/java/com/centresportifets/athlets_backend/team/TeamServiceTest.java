package com.centresportifets.athlets_backend.team;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import com.centresportifets.athlets_backend.auth.AuthService;
import com.centresportifets.athlets_backend.sport.*;
import com.centresportifets.athlets_backend.team.dto.*;
import com.centresportifets.athlets_backend.user.UserType;
import com.centresportifets.athlets_backend.user.athlete.AthleteRepository;
import com.centresportifets.athlets_backend.user.coach.*;
import com.centresportifets.athlets_backend.user.kine.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {
    @Mock AuthService authService;
    @Mock TeamRepository teams;
    @Mock AthleteRepository athletes;
    @Mock AthleteTeamRepository athleteTeams;
    @Mock AthleteTeamDisciplineRepository disciplines;
    @Mock AthleteTeamPositionRepository positions;
    @Mock CoachRepository coaches;
    @Mock SportRepository sports;
    @Mock KineTeamRepository kineTeams;
    @Mock KineRepository kines;
    @Mock Authentication auth;
    @InjectMocks TeamService service;

    @Test
    void createsTeamWithoutAnyStaff() {
        var request = new TeamCreationRequest();
        request.setTeamName("Équipe");
        request.setSportId(1L);
        when(sports.findById(1L)).thenReturn(Optional.of(new Sport()));
        service.createTeam(request, auth);
        verify(teams).save(argThat(team -> team.getName().equals("Équipe")));
        verify(coaches, never()).findById(any());
    }

    @Test
    void promotingAssistantAndRemovingHeadPreservesAccountStatuses() {
        Team team = new Team();
        team.setId(10L);
        Coach previous = new Coach();
        previous.setId(1L);
        previous.setAccountStatus("Active");
        previous.setTeam(team);
        Coach next = new Coach();
        next.setId(2L);
        next.setTeam(team);
        next.setAccountStatus("Pending");
        when(authService.canAccessTeams(auth, List.of(10L))).thenReturn(true);
        when(teams.findById(10L)).thenReturn(Optional.of(team));
        when(coaches.findByTeam_IdAndIsHeadCoachTrue(10L)).thenReturn(previous);
        when(coaches.findByTeam_IdAndIsHeadCoachFalse(10L)).thenReturn(List.of(next));
        when(coaches.findById(2L)).thenReturn(Optional.of(next));
        var request = new TeamModificationRequest();
        request.setNewTeamName("Équipe");
        request.setNewCoachId(2L);
        service.modifyTeam(10L, request, auth);
        assertNull(previous.getTeam());
        assertEquals("Active", previous.getAccountStatus());
        assertSame(team, next.getTeam());
        assertTrue(next.isHeadCoach());
        assertEquals("Pending", next.getAccountStatus());
    }

    @Test
    void coachWithoutTeamGetsEmptyList() {
        when(authService.getAuthenticatedUserType(auth)).thenReturn(UserType.COACH);
        when(auth.getName()).thenReturn("coach");
        when(coaches.findByUsername("coach")).thenReturn(Optional.of(new Coach()));
        assertTrue(service.getTeams(auth).isEmpty());
    }
}
