package com.centresportifets.athlets_backend.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.centresportifets.athlets_backend.user.UserAccount;
import com.centresportifets.athlets_backend.user.UserAccountRepository;
import com.centresportifets.athlets_backend.user.UserStatus;
import com.centresportifets.athlets_backend.user.UserType;
import com.centresportifets.athlets_backend.user.athlete.Athlete;
import com.centresportifets.athlets_backend.user.athlete.AthleteRepository;
import com.centresportifets.athlets_backend.user.coach.Coach;
import com.centresportifets.athlets_backend.user.coach.CoachRepository;
import com.centresportifets.athlets_backend.user.kine.Kine;
import com.centresportifets.athlets_backend.user.kine.KineRepository;
import com.centresportifets.athlets_backend.user.kine.KineTeamRepository;

@RequiredArgsConstructor
@Component("authService")
@Service
public class AuthService {
	private final UserAccountRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final SecurityContextLogoutHandler logoutHandler;
	private final CoachRepository coachRepository;
	private final KineRepository kineRepository;
	private final KineTeamRepository kineTeamRepository;
	private final AthleteRepository athleteRepository;
	private final SecurityContextRepository securityContextRepository =
			new HttpSessionSecurityContextRepository();

	/**
	 * Verifies inbound login attempts.
	 *
	 * @param username Username to be verified
	 * @param rawPassword Unencrypted password
	 * @return the authenticated user object if an account is associated with the credentials
	 */
	public Optional<UserAccount> verifyAndFetchUser(String username, String rawPassword) {
		Optional<UserAccount> user = userRepository.findByUsername(username);

		if (user.isEmpty()) {
			return Optional.empty();
		}

		UserAccount realUser = user.get();

		if(realUser.getAccountStatus() == UserStatus.INACTIVE.getStatus()){
			throw new IllegalStateException("User account is inactive");
		}

		return passwordEncoder.matches(rawPassword, realUser.getPassword())
				? Optional.of(realUser)
				: Optional.empty();
	}

	/**
	 * Logs in the user to springboot, and creates the JSESSIONID token that is sent to the frontend
	 * browser. Checks if the user 
	 *
	 * @param UserAccount authenticated user that has been fetched with the appropriate credentials
	 * @param request the incoming HTTP request used to bind and establish the security context
	 *     session
	 * @param response the outgoing HTTP response where the JSESSIONID cookie is injected upon
	 *     success
	 */
	public void loginUser(
			UserAccount UserAccount, HttpServletRequest request, HttpServletResponse response) {
		Authentication authentication =
				UsernamePasswordAuthenticationToken.authenticated(
						UserAccount.getUsername(), null, List.of());

		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);

		SecurityContextHolder.setContext(context);

		securityContextRepository.saveContext(context, request, response);
	}

	/**
	 * Logs out the user from springboot, and invalidates the JSESSIONID token on the frontend
	 * browser
	 *
	 * @param authentication the current authentication object of the user to be logged out, used to
	 *     invalidate the security context session
	 * @param request the incoming HTTP request used to bind and establish the security context
	 *     session
	 * @param response the outgoing HTTP response where the JSESSIONID cookie is injected upon
	 *     success
	 */
	public void logoutUser(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
		logoutHandler.logout(request, response, authentication);
	}

	/**
	 *  Checks if the userId provided (like the one for completing a test) corresponds to the user connected to the backend
	 */
	public boolean checkIfUserIsAuthenticatedUser(Long userId, Authentication auth) {
		Optional<UserAccount> userOpt = userRepository.findByUsername(auth.getName());
		if (userOpt.isEmpty()) {
			return false;
		}
		UserAccount authenticatedUser = userOpt.get();
		return authenticatedUser.getId().equals(userId);
	}

	public boolean checkIfUserIsAuthenticatedUser(UserAccount user, Authentication auth) {
		return checkIfUserIsAuthenticatedUser(user.getId(), auth);
	}

	public boolean canManageTeams(Authentication auth, List<Long> teamIds) {
		if (teamIds == null || teamIds.isEmpty()) {
			throw new IllegalArgumentException("Team IDs list cannot be null or empty.");
		}

		if (hasPermission(auth, "ADMIN")) {
			return true;
		}

		if (hasPermission(auth, "COACH")) {
			Coach coach = coachRepository.findByUsername(auth.getName())
					.orElseThrow(() -> new IllegalArgumentException("Coach profile not found"));

			if (teamIds.size() > 1) {
				throw new IllegalArgumentException("Coaches can only manage one team at a time.");
			}

			return teamIds.get(0).equals(coach.getTeam().getId());
		}

		if (hasPermission(auth, "KINE")) {
			Kine kine = kineRepository.findByUsername(auth.getName())
					.orElseThrow(() -> new IllegalArgumentException("Kinesiologist profile not found"));

			return teamIds.stream().allMatch(teamId -> kineTeamRepository.existsByKineIdAndTeamId(kine.getId(), teamId));
		}

		return false;
	}

	/**
     * Helper to verify if the authenticated user has either ADMIN role OR 
     * is a COACH who manages ALL of the athletes specified by their usernames.
     */
    public boolean canManageAthletes(Authentication auth, List<String> usernames) {
        if (hasPermission(auth, "ADMIN")) {
            return true;
        }
        
        if (hasPermission(auth, "COACH")) {
            Coach coach = coachRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Coach profile not found"));
            
            List<Athlete> athletes = athleteRepository.findAllByUsernameIn(usernames);
            if (athletes.isEmpty() || athletes.size() != usernames.size()) {
                return false;
            }
            
            Long coachTeamId = coach.getTeam().getId();
            return athletes.stream().allMatch(athlete -> 
                athlete.getAthleteTeams().stream()
                       .anyMatch(at -> at.getId().getTeamId().equals(coachTeamId))
            );
        }

		if (hasPermission(auth, "KINE")) {
			Kine kine = kineRepository.findByUsername(auth.getName())
					.orElseThrow(() -> new IllegalArgumentException("Kinesiologist profile not found"));
		
			List<Athlete> athletes = athleteRepository.findAllByUsernameIn(usernames);
			for (Athlete athlete : athletes) {
				boolean isAssociated = athlete.getAthleteTeams().stream()
						.anyMatch(at -> kineTeamRepository.existsByKineIdAndTeamId(kine.getId(), at.getId().getTeamId()));
				if (!isAssociated) {
					return false;
				}
			}
			
			return true;
		}
        
        return false;
    }

    /**
     * Validates that the currently authenticated user owns the given athlete profile.
     */
    public boolean isAthleteOwner(Authentication auth, Athlete athlete) {
        if (athlete == null || auth == null) return false;
        return checkIfUserIsAuthenticatedUser(athlete.getId(), auth);
    }

    /**
     * Checks if the authenticated user has a specific permission level
     */
    public boolean hasPermission(Authentication auth, String userTypeName) {
        try {
            UserType userType = UserType.valueOf(userTypeName);
            Optional<UserAccount> userOpt = userRepository.findByUsername(auth.getName());
            if (userOpt.isEmpty()) {
                return false;
            }
            UserAccount authenticatedUser = userOpt.get();
            return userType.getPermissionLevel() == (authenticatedUser.getAccessLevel());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

	/**
	 *  Retrieves the usertype of the current authenticated user
	 */
	public UserType getAuthenticatedUserType(Authentication auth){
		int permissionLevel = userRepository.findByUsername(auth.getName()).orElseThrow(() -> new IllegalArgumentException("No user logged in the backend")).getAccessLevel();
		switch (permissionLevel){
			case 1: return UserType.ADMIN;
			case 2: return UserType.COACH;
			case 3: return UserType.ATHLETE;
			case 4: return UserType.KINE;
			default: return UserType.INVALID;
		}
	}

	/**
     * Deactivates a user account based on the caller's role permissions.
     * - Admins can deactivate any account except other Admins.
     * - Coaches can only deactivate Athletes belonging to their own team.
     *
     * @param userId To-be-deactivated target user id
     * @param auth Current authenticated caller session
     */
    public void setUserInactive(Long userId, Authentication auth) {
        UserAccount targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Target user account not found."));
        
        UserType callerType = getAuthenticatedUserType(auth);

        switch (callerType) {
            case ADMIN:
                if (targetUser.getAccessLevel() == UserType.ADMIN.getPermissionLevel()) {
                    throw new AccessDeniedException("Administrators cannot deactivate other admin accounts.");
                }
                break;

            case COACH:
                if (targetUser.getAccessLevel() != UserType.ATHLETE.getPermissionLevel()) 
                    throw new AccessDeniedException("Coaches are only authorized to deactivate athletes.");

				if(!canManageAthletes(auth, List.of(targetUser.getUsername())))
					throw new AccessDeniedException("You can only deactivate athletes belonging to your own team.");

                break;

            default:
                throw new AccessDeniedException("You do not have permission to modify user statuses.");
        }

        targetUser.setAccountStatus(UserStatus.INACTIVE.getStatus());
        userRepository.save(targetUser);
    }
}
