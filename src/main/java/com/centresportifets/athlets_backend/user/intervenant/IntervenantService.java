package com.centresportifets.athlets_backend.user.intervenant;

import java.util.List;
import java.util.UUID;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.centresportifets.athlets_backend.auth.AuthService;
import com.centresportifets.athlets_backend.user.*;
import com.centresportifets.athlets_backend.user.administrator.Administrator;
import com.centresportifets.athlets_backend.user.coach.Coach;
import com.centresportifets.athlets_backend.user.kine.Kine;
import com.centresportifets.athlets_backend.user.kine.KineTeamRepository;
import com.centresportifets.athlets_backend.user.intervenant.dto.*;

@Service
@RequiredArgsConstructor
@Transactional
@PreAuthorize("@authService.hasPermission(authentication, 'ADMIN')")
public class IntervenantService {
    private final UserAccountRepository users;
    private final KineTeamRepository kineTeams;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<IntervenantData> list() {
        return users.findByAccessLevelInOrderByLastNameAscFirstNameAsc(List.of(1, 2, 4))
                .stream().map(this::toData).toList();
    }

    @Transactional(readOnly = true)
    public IntervenantData get(Long id) {
        return toData(find(id));
    }

    public IntervenantData create(IntervenantCreateRequest request) {
        if (request == null || request.role() == null) {
            throw new IllegalArgumentException("Le rôle est obligatoire.");
        }
        UserAccount user = switch (request.role()) {
            case ADMIN -> new Administrator();
            case COACH -> new Coach();
            case KINE -> new Kine();
            default -> throw new IllegalArgumentException("Rôle d'intervenant invalide.");
        };
        setCoordinates(user, request.firstName(), request.lastName(), request.email(), request.phone(), request.username());
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setAccountStatus(UserStatus.PENDING.getStatus());
        user.setAccountActivated(false);
        users.saveAndFlush(user);
        authService.generateActivationTokenForUser(user);
        return toData(user);
    }

    public IntervenantData update(Long id, IntervenantUpdateRequest request) {
        if (request == null) throw new IllegalArgumentException("Les coordonnées sont obligatoires.");
        UserAccount user = find(id);
        String oldEmail = user.getEmail();
        String oldUsername = user.getUsername();
        setCoordinates(user, request.firstName(), request.lastName(), request.email(), request.phone(), request.username());
        if (!java.util.Objects.equals(oldUsername, user.getUsername())) {
            user.setSessionVersion(user.getSessionVersion() + 1);
        }
        users.saveAndFlush(user);
        if (!oldEmail.equals(user.getEmail())) {
            authService.invalidateAccountTokens(user);
            if (UserStatus.PENDING.getStatus().equals(user.getAccountStatus())) {
                authService.generateActivationTokenForUser(user);
            }
        }
        return toData(user);
    }

    public void deactivate(Long id, Authentication auth) {
        find(id);
        authService.setUserInactive(id, auth);
    }

    public IntervenantData reactivate(Long id) {
        UserAccount user = find(id);
        if (!UserStatus.INACTIVE.getStatus().equals(user.getAccountStatus())) {
            throw new IllegalArgumentException("Seul un compte inactif peut être réactivé.");
        }
        user.setAccountStatus(user.isAccountActivated() ? UserStatus.ACTIVE.getStatus() : UserStatus.PENDING.getStatus());
        users.save(user);
        if (!user.isAccountActivated()) authService.generateActivationTokenForUser(user);
        return toData(user);
    }

    public void resendActivation(Long id) {
        authService.generateActivationTokenForUser(find(id));
    }

    private UserAccount find(Long id) {
        UserAccount user = users.findById(id).orElseThrow(() -> new EntityNotFoundException("Intervenant introuvable."));
        if (!List.of(1, 2, 4).contains(user.getAccessLevel())) {
            throw new EntityNotFoundException("Intervenant introuvable.");
        }
        return user;
    }

    private void setCoordinates(UserAccount user, String firstName, String lastName, String email, String phone, String username) {
        firstName = required(firstName, 50, "Prénom");
        lastName = required(lastName, 50, "Nom");
        email = required(email, 254, "Courriel");
        username = required(username, 50, "Nom d'utilisateur");
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) throw new IllegalArgumentException("Courriel invalide.");
        phone = phone == null || phone.isBlank() ? null : phone.trim();
        if (phone != null && phone.length() > 20) throw new IllegalArgumentException("Téléphone trop long (20 caractères maximum).");
        boolean emailExists = user.getId() == null ? users.existsByEmail(email) : users.existsByEmailAndIdNot(email, user.getId());
        boolean usernameExists = user.getId() == null ? users.existsByUsername(username) : users.existsByUsernameAndIdNot(username, user.getId());
        if (emailExists) throw new IllegalArgumentException("Ce courriel est déjà utilisé.");
        if (usernameExists) throw new IllegalArgumentException("Ce nom d'utilisateur est déjà utilisé.");
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setUsername(username);
    }

    private String required(String value, int maxLength, String label) {
        if (value == null || value.isBlank() || value.trim().length() > maxLength) {
            throw new IllegalArgumentException(label + " obligatoire (maximum " + maxLength + " caractères).");
        }
        return value.trim();
    }

    private IntervenantData toData(UserAccount user) {
        UserType role = switch (user.getAccessLevel()) {
            case 1 -> UserType.ADMIN;
            case 2 -> UserType.COACH;
            case 4 -> UserType.KINE;
            default -> throw new IllegalArgumentException("Rôle d'intervenant invalide.");
        };
        List<Long> teamIds = List.of();
        if (user instanceof Coach coach && coach.getTeam() != null) teamIds = List.of(coach.getTeam().getId());
        if (role == UserType.KINE) teamIds = kineTeams.findByKineId(user.getId()).stream().map(link -> link.getTeam().getId()).toList();
        return new IntervenantData(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(),
                user.getPhone(), user.getUsername(), user.getAccountStatus(), user.getAccountCreationDate(),
                user.getAccessLevel(), role, user.isAccountActivated(), teamIds);
    }
}
