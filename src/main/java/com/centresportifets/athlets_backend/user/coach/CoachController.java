package com.centresportifets.athlets_backend.user.coach;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.centresportifets.athlets_backend.user.coach.dto.CoachListDisplay;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Coach controller", description = "Handles coach-related actions")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coach")
public class CoachController {

    private final CoachService coachService;

    @GetMapping("/coaches")
    public ResponseEntity<List<CoachListDisplay>> getCoaches() {
        return ResponseEntity.status(HttpStatus.OK).body(coachService.getCoaches());
    }
}
