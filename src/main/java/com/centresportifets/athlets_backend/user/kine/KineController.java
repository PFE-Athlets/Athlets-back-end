package com.centresportifets.athlets_backend.user.kine;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.centresportifets.athlets_backend.user.kine.dto.KineDisplay;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Kine controller", description = "Handles kine-related actions")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kine")
public class KineController {

    private final KineService kineService;

    @GetMapping("/kinesiologists")
    public ResponseEntity<List<KineDisplay>> getKinesiologists() {
        return ResponseEntity.status(HttpStatus.OK).body(kineService.getKinesiologists());
    }
}
