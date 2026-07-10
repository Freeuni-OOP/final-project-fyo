package com.fyo.onboarding;

import com.fyo.auth.CurrentUserService;
import com.fyo.domain.User;
import com.fyo.onboarding.dto.OnboardingRequest;
import com.fyo.onboarding.dto.OnboardingResponse;
import com.fyo.onboarding.dto.OnboardingStatusResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final CurrentUserService currentUserService;

    public OnboardingController(OnboardingService onboardingService, CurrentUserService currentUserService) {
        this.onboardingService = onboardingService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public OnboardingResponse completeOnboarding(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody OnboardingRequest request
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        return onboardingService.completeOnboarding(currentUser.getId(), request);
    }

    @GetMapping("/status")
    public OnboardingStatusResponse getOnboardingStatus(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        return onboardingService.getOnboardingStatus(currentUser.getId());
    }
}
