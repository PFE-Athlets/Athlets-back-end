package com.centresportifets.athlets_backend.sport.dto;

import java.util.List;

import lombok.Data;

@Data
public class DisciplinesAndPositions {
    List<SportExtraInfo> disciplines;
    List<SportExtraInfo> positions;
}
