package com.fyo.profile;

import com.fyo.auth.CurrentUserService;
import com.fyo.domain.User;
import com.fyo.profile.dto.ProfileResponse;
import com.fyo.profile.dto.UpdateProfileRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;
    private final CurrentUserService currentUserService;

    public ProfileController(ProfileService profileService, CurrentUserService currentUserService) {
        this.profileService = profileService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/me")
    public ProfileResponse getMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        return profileService.getMyProfile(currentUser);
    }

    @PutMapping("/me")
    public ProfileResponse updateMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        return profileService.updateMyProfile(currentUser, request);
    }

    @GetMapping("/{userId}")
    public ProfileResponse getPublicProfile(@PathVariable Long userId) {
        return profileService.getPublicProfile(userId);
    }
}
