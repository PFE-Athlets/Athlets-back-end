package com.centresportifets.athlets_backend.team;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.centresportifets.athlets_backend.team.dto.SubcoachDisplay;
import com.centresportifets.athlets_backend.team.dto.TeamDisplay;
import com.centresportifets.athlets_backend.team.dto.TeamModificationRequest;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Team controller", description = "Handles sports and teams related actions")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/team")
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/teams")
    public ResponseEntity<List<Team>> getTeams(Authentication auth) {
        return ResponseEntity.status(HttpStatus.OK).body(teamService.getTeams(auth));
    }

    @GetMapping("/teams/display")
    public ResponseEntity<List<TeamDisplay>> getTeamDisplays(Authentication auth) {
        return ResponseEntity.status(HttpStatus.OK).body(teamService.getTeamDisplays(auth));
    }

    @GetMapping("/subcoaches/{teamId}")
    public ResponseEntity<List<SubcoachDisplay>> getSubcoaches(@PathVariable Long teamId, Authentication auth) {
        return ResponseEntity.status(HttpStatus.OK).body(teamService.getSubcoaches(teamId, auth));
    }

    @PatchMapping("/modify/{teamId}")
    public ResponseEntity<Object> modifyTeam(@PathVariable Long teamId, @RequestBody TeamModificationRequest request, Authentication auth) {
        teamService.modifyTeam(teamId, request, auth);
        return ResponseEntity.ok().build();
    }
}
