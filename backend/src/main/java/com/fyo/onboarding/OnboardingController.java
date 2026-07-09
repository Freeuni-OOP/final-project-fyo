package com.fyo.onboarding;

import com.fyo.onboarding.dto.OnboardingRequest;
import com.fyo.onboarding.dto.OnboardingResponse;
import com.fyo.onboarding.dto.OnboardingStatusResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    // TODO: replace @RequestParam Long userId with authenticated user from
    //       security context once Sandro's Firebase filter is merged.
    //       @AuthenticationPrincipal FirebaseUserDetails userDetails
    //          and extract userDetails.getUserId()
    @PostMapping
    public OnboardingResponse completeOnboarding(
            @RequestParam Long userId,
            @Valid @RequestBody OnboardingRequest request
    ) {
        return onboardingService.completeOnboarding(userId, request);
    }

    // TODO: same as above. Replace @RequestParam with auth context
    @GetMapping("/status")
    public OnboardingStatusResponse getOnboardingStatus(
            @RequestParam Long userId
    ) {
        return onboardingService.getOnboardingStatus(userId);
    }
}