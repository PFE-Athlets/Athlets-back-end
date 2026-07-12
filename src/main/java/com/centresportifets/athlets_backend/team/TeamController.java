package com.centresportifets.athlets_backend.team;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.centresportifets.athlets_backend.team.dto.TeamDisplay;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Team controller", description = "Handles sports and teams related actions")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/team")
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/teams")
    public ResponseEntity<List<TeamDisplay>> getTeamDisplays(Authentication auth) {
        return ResponseEntity.status(HttpStatus.OK).body(teamService.getTeams(auth));
    }
}
