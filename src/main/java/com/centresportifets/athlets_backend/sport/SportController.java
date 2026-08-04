package com.centresportifets.athlets_backend.sport;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.centresportifets.athlets_backend.sport.dto.DisciplinesAndPositions;
import com.centresportifets.athlets_backend.sport.dto.SportDisplay;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Sport controller", description = "Handles sports-related actions")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sport")
public class SportController {

    private final SportService sportService;

    @GetMapping("/sports")
    public ResponseEntity<List<SportDisplay>> getSports() {
        return ResponseEntity.status(HttpStatus.OK).body(sportService.getSports());
    }

    @GetMapping("/disciplines-positions/{sportId}")
    public ResponseEntity<DisciplinesAndPositions> getDisciplinesAndPositions(Long sportId) {
        return ResponseEntity.status(HttpStatus.OK).body(sportService.getDisciplinesAndPositions(sportId));
    }
}
