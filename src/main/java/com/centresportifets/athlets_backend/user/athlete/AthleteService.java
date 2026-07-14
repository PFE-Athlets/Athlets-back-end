package com.centresportifets.athlets_backend.user.athlete;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.centresportifets.athlets_backend.auth.AuthService;
import com.centresportifets.athlets_backend.sport.discipline.Discipline;
import com.centresportifets.athlets_backend.sport.discipline.DisciplineRepository;
import com.centresportifets.athlets_backend.sport.position.Position;
import com.centresportifets.athlets_backend.sport.position.PositionRepository;
import com.centresportifets.athlets_backend.team.AthleteTeam;
import com.centresportifets.athlets_backend.team.AthleteTeamDiscipline;
import com.centresportifets.athlets_backend.team.AthleteTeamDisciplineRepository;
import com.centresportifets.athlets_backend.team.AthleteTeamPosition;
import com.centresportifets.athlets_backend.team.AthleteTeamPositionRepository;
import com.centresportifets.athlets_backend.team.AthleteTeamRepository;
import com.centresportifets.athlets_backend.team.Team;
import com.centresportifets.athlets_backend.team.TeamRepository;
import com.centresportifets.athlets_backend.user.UserType;
import com.centresportifets.athlets_backend.user.athlete.dto.AthleteCreateRequest;
import com.centresportifets.athlets_backend.user.athlete.dto.AthleteData;
import com.centresportifets.athlets_backend.user.athlete.dto.AthleteUpdateRequest;
import com.centresportifets.athlets_backend.user.coach.Coach;
import com.centresportifets.athlets_backend.user.coach.CoachRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AthleteService {
    private final AthleteRepository athleteRepository;
    private final TeamRepository teamRepository;
    private final AthleteTeamRepository athleteTeamRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final CoachRepository coachRepository;
    private final AthleteTeamPositionRepository athleteTeamPositionRepository;
    private final AthleteTeamDisciplineRepository athleteTeamDisciplineRepository;
    private final PositionRepository positionRepository;
    private final DisciplineRepository disciplineRepository;

    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN') or @authService.hasPermission(authentication, 'COACH')")
    public void createAthlete(AthleteCreateRequest request, Authentication auth) {
        String teamName = request.getAthleteTeamName();
        if (authService.getAuthenticatedUserType(auth) == UserType.COACH) {
            String coachTeamName = coachRepository.findByUsername(auth.getName()).get().getTeam().getName();
            if (!coachTeamName.equals(request.getAthleteTeamName())) {
                throw new IllegalArgumentException("Coaches can only create athletes for their own team.");
            }
        }
        
        Team team = teamRepository.findByName(teamName)
            .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamName));

        Athlete athlete = AthleteMapper.toAthlete(request, passwordEncoder.encode("ChangeMe123!"));
        athlete = athleteRepository.save(athlete);

        AthleteTeam athleteTeam = new AthleteTeam();
        athleteTeam.setAthlete(athlete);
        athleteTeam.setTeam(team);
        // TODO set discipline and position fields if provided
        athleteTeamRepository.save(athleteTeam);
    }

    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN') or @authService.hasPermission(authentication, 'COACH')")
    public List<AthleteData> getAllAthletes(Authentication auth){
        UserType userType = authService.getAuthenticatedUserType(auth);

        if(userType == UserType.ADMIN)
            return athleteRepository.findAll().stream().map((athlete) -> new AthleteData(athlete)).toList();

        Coach coach = coachRepository.findByUsername(auth.getName()).orElseThrow(() -> new IllegalArgumentException("Current coach could not be found."));

        return athleteRepository.findByAthleteTeamsTeamId(coach.getTeam().getId()).stream().map((athlete) -> new AthleteData(athlete)).toList();
    }

    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN')")
    public List<AthleteData> getAthletesForTeam(long teamId){
        return athleteRepository.findByAthleteTeamsTeamId(teamId).stream().map((athlete) -> new AthleteData(athlete)).toList();
    }

    @PreAuthorize("@authService.hasPermission(authentication, 'ATHLETE')")
    public AthleteData getCurrentAthleteData(Authentication auth){
        return new AthleteData(athleteRepository.findByUsername(auth.getName()).orElseThrow(() -> new IllegalArgumentException("Authenticated user is not an athlete")));
    }

    /**
     * Updates an existing athlete's core profile metrics along with their 
     * team positions and sports disciplines.
     */
    @Transactional
    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN') or @authService.hasPermission(authentication, 'COACH')")
    public void updateAthlete(Long athleteId, AthleteUpdateRequest request, Authentication auth) {
        Athlete athlete = athleteRepository.findById(athleteId)
                .orElseThrow(() -> new IllegalArgumentException("Athlete not found with ID: " + athleteId));

        if (!authService.canManageAthletes(auth, List.of(athlete.getUsername()))) {
            throw new AccessDeniedException("You do not have permission to manage this athlete.");
        }

        UserType callerType = authService.getAuthenticatedUserType(auth);

        List<Long> teamsToProcess = callerType == UserType.COACH ? 
            List.of(coachRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new IllegalArgumentException("Current coach could not be found.")).getTeam().getId()) 
            : request.getTeamIds();

        if (request.getPhone() != null) athlete.setPhone(request.getPhone());
        if (request.getWeightKg() != null) athlete.setWeightKg(request.getWeightKg());
        if (request.getInjuryHistory() != null) athlete.setInjuryHistory(request.getInjuryHistory());
        athleteRepository.save(athlete);

        athleteTeamPositionRepository.deleteByPosition_IdNotInAndAthlete_IdAndTeam_IdNotIn(request.getPositionIds(), athlete.getId(), request.getTeamIds());
        athleteTeamDisciplineRepository.deleteByDiscipline_IdNotInAndAthlete_IdAndTeam_IdNotIn(request.getDisciplineIds(), athlete.getId(), request.getTeamIds());

        for (Long teamId : teamsToProcess) {
            Team team = teamRepository.getReferenceById(teamId);

            if (request.getPositionIds() != null) {
                for (Long posId : request.getPositionIds()) {
                    Position position = positionRepository.findById(posId)
                            .orElseThrow(() -> new IllegalArgumentException("Position not found: " + posId));
                    athleteTeamPositionRepository.save(new AthleteTeamPosition(null, athlete, team, position));
                }
            }

            if (request.getDisciplineIds() != null) {
                for (Long discId : request.getDisciplineIds()) {
                    Discipline discipline = disciplineRepository.findById(discId)
                            .orElseThrow(() -> new IllegalArgumentException("Discipline not found: " + discId));
                    athleteTeamDisciplineRepository.save(new AthleteTeamDiscipline(null, athlete, team, discipline));
                }
            }
        }
    }
}
