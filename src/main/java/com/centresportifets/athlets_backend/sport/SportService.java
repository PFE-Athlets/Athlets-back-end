package com.centresportifets.athlets_backend.sport;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.centresportifets.athlets_backend.sport.dto.SportDisplay;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class SportService {
    private final SportRepository sportRepository;

    public List<SportDisplay> getSports() {
        List<SportDisplay> sportDisplays = new ArrayList<>();
        sportRepository.findAll().forEach(sport -> {
            SportDisplay display = new SportDisplay();
            display.setSportId(sport.getId());
            display.setSportName(sport.getName());
            sportDisplays.add(display);
        });
        return sportDisplays;
    }
}
