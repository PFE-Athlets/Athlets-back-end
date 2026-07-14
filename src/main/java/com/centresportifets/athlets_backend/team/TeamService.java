package com.centresportifets.athlets_backend.team;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import com.centresportifets.athlets_backend.auth.AuthService;
import com.centresportifets.athlets_backend.sport.SportRepository;
import com.centresportifets.athlets_backend.team.dto.SubcoachDisplay;
import com.centresportifets.athlets_backend.team.dto.TeamCreationRequest;
import com.centresportifets.athlets_backend.team.dto.TeamDisplay;
import com.centresportifets.athlets_backend.team.dto.TeamModificationRequest;
import com.centresportifets.athlets_backend.user.UserStatus;
import com.centresportifets.athlets_backend.user.UserType;
import com.centresportifets.athlets_backend.user.coach.Coach;
import com.centresportifets.athlets_backend.user.coach.CoachRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TeamService {
    private final AuthService authService;
    private final TeamRepository teamRepository;
    private final AthleteTeamRepository athleteTeamRepository;
    private final CoachRepository coachRepository;
    private final SportRepository sportRepository;

    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN') or @authService.hasPermission(authentication, 'COACH')")
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

    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN') or @authService.hasPermission(authentication, 'COACH')")
    public List<SubcoachDisplay> getSubcoaches(Long teamId, Authentication auth) {
        if (authService.getAuthenticatedUserType(auth) == UserType.COACH) {
            Coach coach = coachRepository.findByUsername(auth.getName()).get();

            if (!coach.getTeam().getId().equals(teamId)) {
                throw new SecurityException("You are not authorized to access this team's subcoaches.");
            }
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
        if (authService.getAuthenticatedUserType(auth) == UserType.COACH) {
            Coach coach = coachRepository.findByUsername(auth.getName()).get();

            if (!coach.getTeam().getId().equals(teamId)) {
                throw new SecurityException("You are not authorized to modify this team.");
            }
        }

        if (request.getNewSubcoachesIds().contains(request.getNewCoachId())) {
            throw new IllegalArgumentException("The head coach cannot be listed as a subcoach.");
        }

        Team team = teamRepository.findById(teamId).orElseThrow(() -> new IllegalArgumentException("Team not found"));
        team.setName(request.getNewTeamName());
        teamRepository.save(team);

        updateTeamCoaches(team, request.getNewCoachId(), request.getNewSubcoachesIds());
    }

    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN')")
    public void createTeam(TeamCreationRequest request, Authentication auth) {
        Team team = new Team();
        team.setName(request.getTeamName());
        team.setSport(sportRepository.findById(request.getSportId()).orElseThrow(() -> new IllegalArgumentException("Sport not found")));
        teamRepository.save(team);

        updateTeamCoaches(team, request.getHeadCoachId(), request.getSubcoachIds());
    }

    private void updateTeamCoaches(Team team, Long newCoachId, List<Long> newSubcoachesIds) {
        Coach previousHeadCoach = coachRepository.findByTeam_IdAndIsHeadCoachTrue(team.getId());
        if (previousHeadCoach != null && !previousHeadCoach.getId().equals(newCoachId)) {
            previousHeadCoach.setTeam(null);
            previousHeadCoach.setSport(null);
            previousHeadCoach.setAccountStatus(UserStatus.INACTIVE.getStatus());
            coachRepository.save(previousHeadCoach);
        }

        Coach newHeadCoach = coachRepository.findById(newCoachId).orElseThrow(() -> new IllegalArgumentException("New head coach not found"));
        newHeadCoach.setTeam(team);
        newHeadCoach.setSport(team.getSport());
        newHeadCoach.setHeadCoach(true);
        newHeadCoach.setAccountStatus(UserStatus.ACTIVE.getStatus());
        coachRepository.save(newHeadCoach);

        List<Coach> previousSubcoaches = coachRepository.findByTeam_IdAndIsHeadCoachFalse(team.getId());
        for (Coach previousSubcoach : previousSubcoaches) {
            if (!newSubcoachesIds.contains(previousSubcoach.getId())) {
                previousSubcoach.setTeam(null);
                previousSubcoach.setSport(null);
                previousSubcoach.setAccountStatus(UserStatus.INACTIVE.getStatus());
                coachRepository.save(previousSubcoach);
            }
        }

        for (Long newSubcoachId : newSubcoachesIds) {
            Coach newSubcoach = coachRepository.findById(newSubcoachId).orElseThrow(() -> new IllegalArgumentException("New subcoach not found"));
            newSubcoach.setTeam(team);
            newSubcoach.setSport(team.getSport());
            newSubcoach.setHeadCoach(false);
            newSubcoach.setAccountStatus(UserStatus.ACTIVE.getStatus());
            coachRepository.save(newSubcoach);
        }
    }
    
    private List<Team> getTeamsForUser(Authentication auth) {
        if (authService.getAuthenticatedUserType(auth) == UserType.ADMIN) {
            return teamRepository.findAll();
        } else {
            return List.of(coachRepository.findByUsername(auth.getName()).get().getTeam());
        }
    }
}
