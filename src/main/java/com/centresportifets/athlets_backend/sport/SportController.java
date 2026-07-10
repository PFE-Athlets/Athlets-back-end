package com.centresportifets.athlets_backend.sport;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.centresportifets.athlets_backend.team.Team;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Sport controller", description = "Handles sports and teams related actions")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sport")
public class SportController {

    private final SportService sportService;

    @GetMapping("/teams")
    public ResponseEntity<List<Team>> getTeams(Authentication auth) {
        return ResponseEntity.status(HttpStatus.OK).body(sportService.getTeams(auth));
    }
}
