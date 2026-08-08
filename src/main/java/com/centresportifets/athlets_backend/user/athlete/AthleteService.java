package com.centresportifets.athlets_backend.user.athlete;

import java.util.ArrayList;
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
import com.centresportifets.athlets_backend.user.athlete.dto.TeamInfoData;
import com.centresportifets.athlets_backend.user.coach.Coach;
import com.centresportifets.athlets_backend.user.coach.CoachRepository;
import com.centresportifets.athlets_backend.user.kine.Kine;
import com.centresportifets.athlets_backend.user.kine.KineRepository;
import com.centresportifets.athlets_backend.user.kine.KineTeamRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AthleteService {
    private final AthleteRepository athleteRepository;
    private final TeamRepository teamRepository;
    private final KineRepository kineRepository;
    private final KineTeamRepository kineTeamRepository;
    private final AthleteTeamRepository athleteTeamRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final CoachRepository coachRepository;
    private final AthleteTeamPositionRepository athleteTeamPositionRepository;
    private final AthleteTeamDisciplineRepository athleteTeamDisciplineRepository;
    private final PositionRepository positionRepository;
    private final DisciplineRepository disciplineRepository;

    @Transactional
    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN') or @authService.hasPermission(authentication, 'COACH') or @authService.hasPermission(authentication, 'KINE')")
    public void createAthlete(AthleteCreateRequest request, Authentication auth) {
        if (!authService.canAccessTeams(auth, request.getTeamsInfo().stream().map(TeamInfoData::getTeamId).toList())) {
            throw new AccessDeniedException("You do not have permission to manage one or more of the specified teams.");
        }

        Athlete athlete = AthleteMapper.toAthlete(request, passwordEncoder.encode("ChangeMe123!"));
        athleteRepository.save(athlete);

        createAthleteTeamsAssociations(athlete, request.getTeamsInfo(), new ArrayList<>(), new ArrayList<>());
    }

    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN') or @authService.hasPermission(authentication, 'COACH') or @authService.hasPermission(authentication, 'KINE')")
    public List<AthleteData> getAllAthletes(Authentication auth){
        UserType userType = authService.getAuthenticatedUserType(auth);

        switch (userType) {
            case ADMIN:
                return athleteRepository.findAll().stream().map((athlete) -> new AthleteData(athlete)).toList();
            case COACH:
                Coach coach = coachRepository.findByUsername(auth.getName()).orElseThrow(() -> new IllegalArgumentException("Current coach could not be found."));
                return athleteRepository.findByAthleteTeamsTeamId(coach.getTeam().getId()).stream().map((athlete) -> new AthleteData(athlete)).toList();
            case KINE:
                Kine kine = kineRepository.findByUsername(auth.getName()).orElseThrow(() -> new IllegalArgumentException("Current coach could not be found."));
                List<Athlete> athletes = new ArrayList<>();
                kineTeamRepository.findByKineId(kine.getId())
                    .forEach(kineTeam -> {
                        Team team = kineTeam.getTeam();
                        List<Athlete> teamAthletes = athleteRepository.findByAthleteTeamsTeamId(team.getId());
                        athletes.addAll(teamAthletes);
                    });
                return athletes.stream().map((athlete) -> new AthleteData(athlete)).toList();
            default:
                throw new SecurityException("You do not have permission to view teams.");
        }
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
    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN') or @authService.hasPermission(authentication, 'COACH') or @authService.hasPermission(authentication, 'KINE')")
    public void updateAthlete(Long athleteId, AthleteUpdateRequest request, Authentication auth) {
        Athlete athlete = athleteRepository.findById(athleteId)
                .orElseThrow(() -> new IllegalArgumentException("Athlete not found with ID: " + athleteId));

        if (!authService.canManageAthletes(auth, List.of(athlete.getUsername()))) {
            throw new AccessDeniedException("You do not have permission to manage this athlete.");
        }

        List<Long> teamIds = request.getTeamsInfo().stream().map(TeamInfoData::getTeamId).toList();
        if (!authService.canAccessTeams(auth, teamIds)) {
            throw new AccessDeniedException("You do not have permission to manage one or more of the specified teams.");
        }

        athlete.setPhone(request.getPhone());
        athlete.setWeightKg(request.getWeightKg());
        athlete.setInjuryHistory(request.getInjuryHistory());
        athleteRepository.save(athlete);

        athleteTeamRepository.deleteByAthlete_IdAndTeam_IdNotIn(athleteId, teamIds);

        List<Long> updatedOrNewDisciplineIds = new ArrayList<>();
        List<Long> updatedOrNewPositionIds = new ArrayList<>();
        createAthleteTeamsAssociations(athlete, request.getTeamsInfo(), updatedOrNewDisciplineIds, updatedOrNewPositionIds);

        athleteTeamDisciplineRepository.deleteByAthlete_IdAndIdNotIn(athleteId, updatedOrNewDisciplineIds);
        athleteTeamPositionRepository.deleteByAthlete_IdAndIdNotIn(athleteId, updatedOrNewPositionIds);
    }

    @Transactional
    private void createAthleteTeamsAssociations(Athlete athlete, List<TeamInfoData> teamsInfo, List<Long> updatedOrNewDisciplineIds, List<Long> updatedOrNewPositionIds) {
        for (TeamInfoData teamInfo : teamsInfo) {
            Team team = teamRepository.findById(teamInfo.getTeamId())
                    .orElseThrow(() -> new IllegalArgumentException("Team id not found: " + teamInfo.getTeamId()));
            
            athleteTeamRepository.createIfNotExists(athlete.getId(), team.getId());

            if (teamInfo.getPositionId() != null) {
                Position position = positionRepository.findById(teamInfo.getPositionId())
                        .orElseThrow(() -> new IllegalArgumentException("Position not found: " + teamInfo.getPositionId()));
                if (position.getSport().getId() != team.getSport().getId()) {
                    throw new IllegalArgumentException("Position does not belong to the same sport as the team.");
                }
    
                athleteTeamPositionRepository.createIfNotExists(athlete.getId(), position.getId(), team.getId());
                AthleteTeamPosition athleteTeamPosition = athleteTeamPositionRepository.findByAthlete_IdAndPosition_IdAndTeam_Id(athlete.getId(), position.getId(), team.getId());
                updatedOrNewPositionIds.add(athleteTeamPosition.getId());
            }

            if (teamInfo.getDisciplineId() != null) {
                Discipline discipline = disciplineRepository.findById(teamInfo.getDisciplineId())
                        .orElseThrow(() -> new IllegalArgumentException("Discipline not found: " + teamInfo.getDisciplineId()));
                if (discipline.getSport().getId() != team.getSport().getId()) {
                    throw new IllegalArgumentException("Discipline does not belong to the same sport as the team.");
                }
    
                athleteTeamDisciplineRepository.createIfNotExists(athlete.getId(), discipline.getId(), team.getId());
                AthleteTeamDiscipline athleteTeamDiscipline = athleteTeamDisciplineRepository.findByAthlete_IdAndDiscipline_IdAndTeam_Id(athlete.getId(), discipline.getId(), team.getId());
                updatedOrNewDisciplineIds.add(athleteTeamDiscipline.getId());
            }
        }
    }
}
