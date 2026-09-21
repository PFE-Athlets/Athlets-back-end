package com.centresportifets.athlets_backend.user.athlete;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import com.centresportifets.athlets_backend.auth.AuthService;
import com.centresportifets.athlets_backend.team.*;
import com.centresportifets.athlets_backend.user.athlete.dto.AthleteUpdateRequest;

@ExtendWith(MockitoExtension.class)
class AthleteAccessTest {
    @Mock AthleteRepository athletes;
    @Mock AuthService authService;
    @Mock AthleteTeamRepository teams;
    @Mock AthleteTeamPositionRepository positions;
    @Mock AthleteTeamDisciplineRepository disciplines;
    @Mock Authentication auth;
    @InjectMocks AthleteService service;

    @Test
    void cannotReadAthletesFromUnassignedTeam() {
        assertThrows(AccessDeniedException.class, () -> service.getAthletesForTeam(7L, auth));
        verifyNoInteractions(athletes);
    }

    @Test
    void removingOwnTeamPreservesOtherTeamsPositionsAndDisciplines() {
        Athlete athlete = new Athlete();
        athlete.setId(20L);
        athlete.setUsername("athlete");
        when(athletes.findById(20L)).thenReturn(Optional.of(athlete));
        when(authService.canManageAthletes(auth, List.of("athlete"))).thenReturn(true);
        when(authService.accessibleTeamIds(auth)).thenReturn(List.of(1L));
        when(teams.findByAthleteId(20L)).thenReturn(List.of(link(1L), link(2L)));
        var request = new AthleteUpdateRequest();
        request.setTeamsInfo(List.of());
        service.updateAthlete(20L, request, auth);
        verify(teams).deleteByAthlete_IdAndTeam_IdIn(20L, List.of(1L));
        verify(positions).deleteByAthlete_IdAndTeam_IdIn(20L, List.of(1L));
        verify(disciplines).deleteByAthlete_IdAndTeam_IdIn(20L, List.of(1L));
        verifyNoMoreInteractions(positions, disciplines);
    }

    private AthleteTeam link(Long id) {
        Team team = new Team(); team.setId(id);
        AthleteTeam link = new AthleteTeam(); link.setTeam(team);
        return link;
    }
}
