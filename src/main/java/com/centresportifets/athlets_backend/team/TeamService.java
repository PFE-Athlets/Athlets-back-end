package com.centresportifets.athlets_backend.team;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import com.centresportifets.athlets_backend.auth.AuthService;
import com.centresportifets.athlets_backend.sport.SportRepository;
import com.centresportifets.athlets_backend.team.dto.AthletePreviewDisplay;
import com.centresportifets.athlets_backend.team.dto.SubcoachDisplay;
import com.centresportifets.athlets_backend.team.dto.TeamCreationRequest;
import com.centresportifets.athlets_backend.team.dto.TeamDisplay;
import com.centresportifets.athlets_backend.team.dto.TeamModificationRequest;
import com.centresportifets.athlets_backend.user.UserStatus;
import com.centresportifets.athlets_backend.user.athlete.Athlete;
import com.centresportifets.athlets_backend.user.athlete.AthleteRepository;
import com.centresportifets.athlets_backend.user.coach.Coach;
import com.centresportifets.athlets_backend.user.coach.CoachRepository;
import com.centresportifets.athlets_backend.user.kine.Kine;
import com.centresportifets.athlets_backend.user.kine.KineRepository;
import com.centresportifets.athlets_backend.user.kine.KineTeam;
import com.centresportifets.athlets_backend.user.kine.KineTeamRepository;
import com.centresportifets.athlets_backend.user.kine.dto.KineDisplay;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TeamService {
    private final AuthService authService;
    private final TeamRepository teamRepository;
    private final AthleteRepository athleteRepository;
    private final AthleteTeamRepository athleteTeamRepository;
    private final AthleteTeamDisciplineRepository athleteTeamDisciplineRepository;
    private final AthleteTeamPositionRepository athleteTeamPositionRepository;
    private final CoachRepository coachRepository;
    private final SportRepository sportRepository;
    private final KineTeamRepository kineTeamRepository;
    private final KineRepository kineRepository;

    public List<TeamDisplay> getTeams(Authentication auth) {
        List<TeamDisplay> teamDisplays = getTeamsForUser(auth).stream().map(team -> {
            TeamDisplay teamDisplay = new TeamDisplay();
            teamDisplay.setTeam(team);
            teamDisplay.setNumberOfAthletes(athleteTeamRepository.countByTeamId(team.getId()));
            
            Coach headCoach = coachRepository.findByTeam_IdAndIsHeadCoachTrue(team.getId());
            if (headCoach != null) {
                teamDisplay.setHeadCoachName(headCoach.getFirstName() + " " + headCoach.getLastName());
                teamDisplay.setHeadCoachId(headCoach.getId());
            }   
            return teamDisplay;
        }).toList();

        return teamDisplays;
    }

    public List<SubcoachDisplay> getSubcoaches(Long teamId, Authentication auth) {
        if (!authService.canAccessTeams(auth, List.of(teamId))) {
            throw new SecurityException("You are not authorized to access this team's subcoaches.");
        }

        List<SubcoachDisplay> subcoaches = new ArrayList<>();
        coachRepository.findByTeam_IdAndIsHeadCoachFalse(teamId).forEach(coach -> {
            SubcoachDisplay subcoachDisplay = new SubcoachDisplay();
            subcoachDisplay.setSubcoachName(coach.getFirstName() + " " + coach.getLastName());
            subcoachDisplay.setCoachId(coach.getId());
            subcoaches.add(subcoachDisplay);
        });

        return subcoaches;
    }

    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN') or @authService.hasPermission(authentication, 'COACH')")
    public void modifyTeam(Long teamId, TeamModificationRequest request, Authentication auth) {
        if (!authService.canAccessTeams(auth, List.of(teamId))) {
            throw new SecurityException("You are not authorized to modify this team.");
        }

        if (request.getNewSubcoachesIds().contains(request.getNewCoachId())) {
            throw new IllegalArgumentException("The head coach cannot be listed as a subcoach.");
        }

        Team team = teamRepository.findById(teamId).orElseThrow(() -> new IllegalArgumentException("Team not found"));
        team.setName(request.getNewTeamName());
        teamRepository.save(team);

        updateTeamCoaches(team, request.getNewCoachId(), request.getNewSubcoachesIds());
        updateTeamKinesiologists(team, request.getNewKinesiologistsIds());
    }

    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN')")
    public void createTeam(TeamCreationRequest request, Authentication auth) {
        Team team = new Team();
        team.setName(request.getTeamName());
        team.setSport(sportRepository.findById(request.getSportId()).orElseThrow(() -> new IllegalArgumentException("Sport not found")));
        teamRepository.save(team);

        updateTeamCoaches(team, request.getHeadCoachId(), request.getSubcoachIds());
        updateTeamKinesiologists(team, request.getKineIds());
    }

    private void updateTeamCoaches(Team team, Long newCoachId, List<Long> newSubcoachesIds) {
        Coach previousHeadCoach = coachRepository.findByTeam_IdAndIsHeadCoachTrue(team.getId());
        if (previousHeadCoach != null && !previousHeadCoach.getId().equals(newCoachId)) {
            setCoach(previousHeadCoach, null, false);
        }

        Coach newHeadCoach = coachRepository.findById(newCoachId).orElseThrow(() -> new IllegalArgumentException("New head coach not found"));
        setCoach(newHeadCoach, team, true);

        List<Coach> previousSubcoaches = coachRepository.findByTeam_IdAndIsHeadCoachFalse(team.getId());
        for (Coach previousSubcoach : previousSubcoaches) {
            if (!newSubcoachesIds.contains(previousSubcoach.getId())) {
                setCoach(previousSubcoach, null, false);
            }
        }

        for (Long newSubcoachId : newSubcoachesIds) {
            Coach newSubcoach = coachRepository.findById(newSubcoachId).orElseThrow(() -> new IllegalArgumentException("New subcoach not found"));
            setCoach(newSubcoach, team, false);
        }
    }

    private void setCoach(Coach coach, Team team, boolean isHeadCoach) {
        coach.setTeam(team);
        coach.setSport(team != null ? team.getSport() : null);
        coach.setHeadCoach(isHeadCoach);
        coach.setAccountStatus(team != null ? UserStatus.ACTIVE.getStatus() : UserStatus.INACTIVE.getStatus());
        coachRepository.save(coach);
    }

    private void updateTeamKinesiologists(Team team, List<Long> newKineIds) {
        List<Kine> previousKinesiologists = kineTeamRepository.findByTeamId(team.getId())
                .stream()
                .map(kineTeam -> kineTeam.getKine())
                .toList();

        for (Kine previousKinesiologist : previousKinesiologists) {
            if (!newKineIds.contains(previousKinesiologist.getId())) {
                kineTeamRepository.deleteByKineIdAndTeamId(previousKinesiologist.getId(), team.getId());
            }
        }

        for (Long newKinesiologistId : newKineIds) {
            if (!kineTeamRepository.existsByKineIdAndTeamId(newKinesiologistId, team.getId())) {
                Kine newKinesiologist = kineRepository.findById(newKinesiologistId)
                        .orElseThrow(() -> new IllegalArgumentException("New kinesiologist not found"));
                KineTeam kineTeam = new KineTeam();
                kineTeam.setKine(newKinesiologist);
                kineTeam.setTeam(team);
                kineTeamRepository.save(kineTeam);
            }
        }
    }
    
    private List<Team> getTeamsForUser(Authentication auth) {
        switch (authService.getAuthenticatedUserType(auth)) {
            case ADMIN:
                return teamRepository.findAll();
            case COACH:
                Coach coach = coachRepository.findByUsername(auth.getName()).orElseThrow(() -> new IllegalArgumentException("Current coach could not be found."));
                return List.of(coach.getTeam());
            case KINE:
                Kine kine = kineRepository.findByUsername(auth.getName()).orElseThrow(() -> new IllegalArgumentException("Current coach could not be found."));
                return kineTeamRepository.findByKineId(kine.getId())
                    .stream()
                    .map(kineTeam -> kineTeam.getTeam())
                    .toList();
            case ATHLETE:
                Athlete athlete = athleteRepository.findByUsername(auth.getName()).orElseThrow(() -> new IllegalArgumentException("Current athlete could not be found."));
                return athleteTeamRepository.findByAthleteId(athlete.getId())
                    .stream()
                    .map(athleteTeam -> athleteTeam.getTeam())
                    .toList();
            default:
                throw new SecurityException("You do not have permission to view teams.");
        }
    }

    public List<KineDisplay> getKinesiologistsByTeamId(Long teamId, Authentication auth) {
        if (!authService.canAccessTeams(auth, List.of(teamId))) {
            throw new SecurityException("You are not authorized to access this team's kinesiologists.");
        }

        List<KineDisplay> kinesiologists = new ArrayList<>();
        kineTeamRepository.findByTeamId(teamId).forEach(kineTeam -> {
            KineDisplay kineDisplay = new KineDisplay();
            kineDisplay.setKineId(kineTeam.getKine().getId());
            kineDisplay.setKineName(kineTeam.getKine().getFirstName() + " " + kineTeam.getKine().getLastName());
            kinesiologists.add(kineDisplay);
        });
        return kinesiologists;
    } 

    public List<AthletePreviewDisplay> getAthletesPreview(Long teamId, Authentication auth) {
        if (!authService.canAccessTeams(auth, List.of(teamId))) {
            throw new SecurityException("You are not authorized to access this team's athletes.");
        }

        List<AthletePreviewDisplay> athletePreviews = new ArrayList<>();
        athleteTeamRepository.findByTeamId(teamId).forEach(athleteTeam -> {
            Athlete athlete = athleteTeam.getAthlete();
            AthletePreviewDisplay athletePreview = new AthletePreviewDisplay();
            athletePreview.setAthleteId(athlete.getId());
            athletePreview.setAthleteName(athlete.getFirstName() + " " + athlete.getLastName());

            // DisciplinesAndPositions disciplinesAndPositions = new DisciplinesAndPositions();
            // athleteTeamDisciplineRepository.findByAthlete_IdAndTeam_Id(athlete.getId(), teamId)
            //     .stream().map(atd -> atd.getDiscipline())
            //     .forEach(discipline -> disciplinesAndPositions.getDisciplines().add(discipline));
            // disciplinesAndPositions.setPositions(athleteTeamPositionRepository.findByAthlete_IdAndTeam_Id(athlete.getId(), teamId).stream().map(atp -> atp.getPosition()).toList());

            athletePreviews.add(athletePreview);
        });
        return athletePreviews;
    }
}
