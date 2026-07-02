package com.centresportifets.athlets_backend.sport;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import com.centresportifets.athlets_backend.auth.AuthService;
import com.centresportifets.athlets_backend.auth.userTypes.CoachRepository;
import com.centresportifets.athlets_backend.auth.userTypes.UserType;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class SportService {
    private final AuthService authService;
    private final TeamRepository teamRepository;
    private final CoachRepository coachRepository;

    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN') or @authService.hasPermission(authentication, 'COACH')")
    public List<String> getTeamNames(Authentication auth) {
        if (authService.getAuthenticatedUserType(auth) == UserType.ADMIN) {
            return teamRepository.findAll().stream()
                    .filter(Objects::nonNull)
                    .map(team -> team.getName())
                    .collect(Collectors.toList());
        } else {
            return List.of(coachRepository.findByUsername(auth.getName()).get().getTeam().getName());
        }
    }
}
