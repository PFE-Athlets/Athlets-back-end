package com.centresportifets.athlets_backend.user.athlete;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.centresportifets.athlets_backend.user.athlete.dto.AthleteCreateRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Tag(name = "Athlete controller", description = "Handles athlete creation and related operations")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/athlete")
public class AthleteController {

    private final AthleteService athleteService;

    @PostMapping("/create")
    public ResponseEntity<?> createAthlete(Authentication auth, @RequestBody AthleteCreateRequest request) {
        athleteService.createAthlete(request, auth);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public String getAllAthletes(@RequestParam String param) {
        return new String();
    }

    @PutMapping("/")
    
}
