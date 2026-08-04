package com.centresportifets.athlets_backend.user.kine;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.centresportifets.athlets_backend.user.kine.dto.KineDisplay;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class KineService {
    private final KineRepository kineRepository;

    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN') or @authService.hasPermission(authentication, 'COACH')")
    public List<KineDisplay> getKinesiologists() {
        return kineRepository.findAll().stream().map(kine -> {
            KineDisplay display = new KineDisplay();
            display.setKineId(kine.getId());
            display.setKineName(kine.getFirstName() + " " + kine.getLastName());
            return display;
        }).toList();
    }
}
