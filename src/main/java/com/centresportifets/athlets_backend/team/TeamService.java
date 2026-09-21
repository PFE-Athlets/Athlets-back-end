package com.centresportifets.athlets_backend.team;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import com.centresportifets.athlets_backend.auth.AuthService;
import com.centresportifets.athlets_backend.sport.SportRepository;
import com.centresportifets.athlets_backend.sport.dto.DisciplinesAndPositions;
import com.centresportifets.athlets_backend.sport.dto.SportExtraInfo;
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
            throw new AccessDeniedException("You are not authorized to access this team's subcoaches.");
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

    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN')")
    @Transactional
    public void modifyTeam(Long teamId, TeamModificationRequest request, Authentication auth) {
        if (!authService.canAccessTeams(auth, List.of(teamId))) {
            throw new AccessDeniedException("You are not authorized to modify this team.");
        }


        Team team = teamRepository.findById(teamId).orElseThrow(() -> new IllegalArgumentException("Team not found"));
        team.setName(validName(request.getNewTeamName()));
        teamRepository.save(team);

        updateTeamCoaches(team, request.getNewCoachId(), request.getNewSubcoachesIds());
        updateTeamKinesiologists(team, request.getNewKinesiologistsIds());
    }

    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN')")
    @Transactional
    public void createTeam(TeamCreationRequest request, Authentication auth) {
        Team team = new Team();
        team.setName(validName(request.getTeamName()));
        team.setSport(sportRepository.findById(request.getSportId()).orElseThrow(() -> new IllegalArgumentException("Sport not found")));
        teamRepository.save(team);

        updateTeamCoaches(team, request.getHeadCoachId(), request.getSubcoachIds());
        updateTeamKinesiologists(team, request.getKineIds());
    }

    private String validName(String name) {
        if (name == null || name.isBlank() || name.trim().length() > 50) {
            throw new IllegalArgumentException("Nom d'équipe obligatoire (50 caractères maximum).");
        }
        return name.trim();
    }

    private List<Long> idsOrEmpty(List<Long> ids) {
        if (ids == null) return List.of();
        if (ids.stream().anyMatch(java.util.Objects::isNull) || ids.stream().distinct().count() != ids.size()) {
            throw new IllegalArgumentException("Les identifiants doivent être uniques et non nuls.");
        }
        return ids;
    }

    private void updateTeamCoaches(Team team, Long newCoachId, List<Long> subcoachIds) {
        List<Long> newSubcoachesIds = idsOrEmpty(subcoachIds);
        if (newCoachId != null && newSubcoachesIds.contains(newCoachId)) {
            throw new IllegalArgumentException("Le coach principal ne peut pas être un adjoint.");
        }
        // Remove previous assignments first, including a former head coach becoming an assistant.
        Coach previousHeadCoach = coachRepository.findByTeam_IdAndIsHeadCoachTrue(team.getId());
        List<Coach> previousSubcoaches = coachRepository.findByTeam_IdAndIsHeadCoachFalse(team.getId());
        if (previousHeadCoach != null) setCoach(previousHeadCoach, null, false);
        previousSubcoaches.forEach(coach -> setCoach(coach, null, false));
        if (newCoachId != null) {
            Coach headCoach = coachRepository.findById(newCoachId)
                    .orElseThrow(() -> new IllegalArgumentException("Coach principal introuvable."));
            setCoach(headCoach, team, true);
        }
        for (Long id : newSubcoachesIds) {
            Coach coach = coachRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Coach adjoint introuvable."));
            setCoach(coach, team, false);
        }
    }

    private void setCoach(Coach coach, Team team, boolean isHeadCoach) {
        coach.setTeam(team);
        coach.setSport(team != null ? team.getSport() : null);
        coach.setHeadCoach(isHeadCoach);
        coachRepository.save(coach);
    }

    private void updateTeamKinesiologists(Team team, List<Long> kineIds) {
        List<Long> newKineIds = idsOrEmpty(kineIds);
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
                return coach.getTeam() == null ? List.of() : List.of(coach.getTeam());
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
                throw new AccessDeniedException("You do not have permission to view teams.");
        }
    }

    public List<KineDisplay> getKinesiologistsByTeamId(Long teamId, Authentication auth) {
        if (!authService.canAccessTeams(auth, List.of(teamId))) {
            throw new AccessDeniedException("You are not authorized to access this team's kinesiologists.");
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
            throw new AccessDeniedException("You are not authorized to access this team's athletes.");
        }

        List<AthletePreviewDisplay> athletePreviews = new ArrayList<>();
        athleteTeamRepository.findByTeamId(teamId).forEach(athleteTeam -> {
            Athlete athlete = athleteTeam.getAthlete();
            AthletePreviewDisplay athletePreview = new AthletePreviewDisplay();
            athletePreview.setAthleteId(athlete.getId());
            athletePreview.setAthleteName(athlete.getFirstName() + " " + athlete.getLastName());

            DisciplinesAndPositions disciplinesAndPositions = new DisciplinesAndPositions();
            List<SportExtraInfo> disciplines = new ArrayList<>();
            athleteTeamDisciplineRepository.findByAthlete_IdAndTeam_Id(athlete.getId(), teamId)
            .stream().map(atd -> atd.getDiscipline())
            .forEach(discipline -> {
                SportExtraInfo info = new SportExtraInfo();
                info.setId(discipline.getId());
                info.setName(discipline.getName());
                disciplines.add(info);
            });
            disciplinesAndPositions.setDisciplines(disciplines);

            List<SportExtraInfo> positions = new ArrayList<>();
            athleteTeamPositionRepository.findByAthlete_IdAndTeam_Id(athlete.getId(), teamId)
            .stream().map(atp -> atp.getPosition())
            .forEach(position -> {
                SportExtraInfo info = new SportExtraInfo();
                info.setId(position.getId());
                info.setName(position.getName());
                positions.add(info);
            });
            disciplinesAndPositions.setPositions(positions);
            athletePreview.setDisciplinesAndPositions(disciplinesAndPositions);

            athletePreviews.add(athletePreview);
        });
        return athletePreviews;
    }
}
