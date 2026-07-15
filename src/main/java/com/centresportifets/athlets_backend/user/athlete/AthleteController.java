package com.centresportifets.athlets_backend.user.athlete;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.centresportifets.athlets_backend.user.athlete.dto.AthleteCreateRequest;
import com.centresportifets.athlets_backend.user.athlete.dto.AthleteData;
import com.centresportifets.athlets_backend.user.athlete.dto.AthleteUpdateRequest;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

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

    @GetMapping("/all")
    public List<AthleteData> getAllAthletesForUser(Authentication auth) {
        return athleteService.getAllAthletes(auth);
    }

    @GetMapping("/team/{teamId}")
    public List<AthleteData> getAthletesForTeam(@PathVariable long teamId){
        return athleteService.getAthletesForTeam(teamId);
    }

    @GetMapping("/current")
    public AthleteData getAthleteData(Authentication auth){
        return athleteService.getCurrentAthleteData(auth);
    }

    @PutMapping("/{id}")
    public void modifyAthlete(@PathVariable("id") Long athleteId, @RequestBody AthleteUpdateRequest request, Authentication auth) 
    {
        athleteService.updateAthlete(athleteId, request, auth);
    }

    @PutMapping("/{athleteId}/deactivate")
    public ResponseEntity<Void> deactivateAthleteAccount(@PathVariable Long athleteId, Authentication auth)
    {
        athleteService.deactivateAthleteAccount(athleteId, auth);
        return ResponseEntity.ok().build();
    }
}