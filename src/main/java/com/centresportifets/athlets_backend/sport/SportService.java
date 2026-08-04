package com.centresportifets.athlets_backend.sport;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.centresportifets.athlets_backend.sport.discipline.Discipline;
import com.centresportifets.athlets_backend.sport.discipline.DisciplineRepository;
import com.centresportifets.athlets_backend.sport.dto.DisciplinesAndPositions;
import com.centresportifets.athlets_backend.sport.dto.SportDisplay;
import com.centresportifets.athlets_backend.sport.dto.SportExtraInfo;
import com.centresportifets.athlets_backend.sport.position.Position;
import com.centresportifets.athlets_backend.sport.position.PositionRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class SportService {
    private final SportRepository sportRepository;
    private final DisciplineRepository disciplineRepository;
    private final PositionRepository positionRepository;

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


    public DisciplinesAndPositions getDisciplinesAndPositions(Long sportId) {
        if (!sportRepository.existsById(sportId)) {
            throw new IllegalArgumentException("Sport with ID " + sportId + " does not exist.");
        }

        List<Discipline> disciplines = disciplineRepository.findBySport_Id(sportId);
        List<Position> positions = positionRepository.findBySport_Id(sportId);
        
        DisciplinesAndPositions result = new DisciplinesAndPositions();
        result.setDisciplines(disciplines.stream().map(discipline -> {
            SportExtraInfo info = new SportExtraInfo();
            info.setId(discipline.getId());
            info.setName(discipline.getName());
            return info;
        }).toList());

        result.setPositions(positions.stream().map(position -> {
            SportExtraInfo info = new SportExtraInfo();
            info.setId(position.getId());
            info.setName(position.getName());
            return info;
        }).toList());

        return result;
    }
}
