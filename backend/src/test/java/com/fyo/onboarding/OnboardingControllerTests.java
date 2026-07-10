package com.fyo.onboarding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fyo.auth.CurrentUserService;
import com.fyo.domain.User;
import com.fyo.onboarding.dto.OnboardingRequest;
import com.fyo.onboarding.dto.OnboardingResponse;
import com.fyo.onboarding.dto.OnboardingStatusResponse;
import com.fyo.onboarding.dto.UserSportDto;
import com.fyo.domain.Sex;
import java.util.List;
import org.junit.jupiter.api.Test;

class OnboardingControllerTests {

    private final OnboardingService onboardingService = mock(OnboardingService.class);
    private final CurrentUserService currentUserService = mock(CurrentUserService.class);
    private final OnboardingController controller = new OnboardingController(onboardingService, currentUserService);

    private static final User CURRENT = new User("uid-1", "Ana", "Kobalia", "ana", "ana@example.com");

    @Test
    void completeOnboardingUsesAuthenticatedUser() {
        OnboardingRequest body = new OnboardingRequest(
                "Ana", "Kobalia", "ana", 22, Sex.FEMALE.name(), "Tbilisi", null,
                List.of(new UserSportDto(1L, "BEGINNER")));
        OnboardingResponse response = mock(OnboardingResponse.class);

        when(currentUserService.requireCurrentUser("Bearer token")).thenReturn(CURRENT);
        when(onboardingService.completeOnboarding(CURRENT.getId(), body)).thenReturn(response);

        assertThat(controller.completeOnboarding("Bearer token", body)).isEqualTo(response);
        verify(onboardingService).completeOnboarding(CURRENT.getId(), body);
    }

    @Test
    void getStatusUsesAuthenticatedUser() {
        OnboardingStatusResponse response = new OnboardingStatusResponse(false);
        when(currentUserService.requireCurrentUser("Bearer token")).thenReturn(CURRENT);
        when(onboardingService.getOnboardingStatus(CURRENT.getId())).thenReturn(response);

        assertThat(controller.getOnboardingStatus("Bearer token")).isEqualTo(response);
    }
}
