package com.centresportifets.athlets_backend.user.athlete;

import com.centresportifets.athlets_backend.user.UserType;
import com.centresportifets.athlets_backend.user.athlete.dto.AthleteCreateRequest;

public class AthleteMapper {
    public static Athlete toAthlete(AthleteCreateRequest request, String password) {
        Athlete athlete = new Athlete();
        athlete.setFirstName(request.getFirstName());
        athlete.setLastName(request.getLastName());
        athlete.setEmail(request.getEmail());
        athlete.setPhone(request.getPhone());
        athlete.setUsername(request.getUsername());
        athlete.setPassword(password);
        athlete.setAccountStatus(request.getAccountStatus());
        athlete.setBirthDate(request.getBirthDate());
        athlete.setGender(request.getGender());
        athlete.setHeightMeters(request.getHeightMeters());
        athlete.setWeightKg(request.getWeightKg());
        athlete.setDominantArm(request.getDominantArm());
        athlete.setDominantLeg(request.getDominantLeg());
        athlete.setInjuryHistory(request.getInjuryHistory());
        athlete.setAccessLevel(UserType.ATHLETE.getPermissionLevel());
        return athlete;
    }
}
