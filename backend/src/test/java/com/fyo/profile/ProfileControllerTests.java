package com.fyo.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fyo.auth.CurrentUserService;
import com.fyo.domain.User;
import com.fyo.profile.dto.ProfileResponse;
import com.fyo.profile.dto.ProfileSportUpdateDto;
import com.fyo.profile.dto.UpdateProfileRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class ProfileControllerTests {

    private final ProfileService profileService = mock(ProfileService.class);
    private final CurrentUserService currentUserService = mock(CurrentUserService.class);
    private final ProfileController controller =
            new ProfileController(profileService, currentUserService);

    private static final ProfileResponse SOME_PROFILE = new ProfileResponse(
            1L, "ana", "Ana", "Kobalia", (short) 22, "FEMALE", "Tbilisi",
            null, "ana@example.com", 4.5, List.of(), List.of(), List.of()
    );

    @Test
    void getMyProfileUsesAuthenticatedUser() {
        User user = new User("uid-1", "Ana", "Kobalia", "ana", "ana@example.com");
        when(currentUserService.requireCurrentUser("Bearer token")).thenReturn(user);
        when(profileService.getMyProfile(user)).thenReturn(SOME_PROFILE);

        ProfileResponse response = controller.getMyProfile("Bearer token");

        assertThat(response).isEqualTo(SOME_PROFILE);
        verify(profileService).getMyProfile(user);
    }

    @Test
    void updateMyProfileUsesAuthenticatedUser() {
        User user = new User("uid-1", "Ana", "Kobalia", "ana", "ana@example.com");
        UpdateProfileRequest request = new UpdateProfileRequest(
                "Ana", "Kobalia", "ana", 22, "FEMALE", "Tbilisi", null,
                List.of(new ProfileSportUpdateDto(1L, "BEGINNER"))
        );
        when(currentUserService.requireCurrentUser("Bearer token")).thenReturn(user);
        when(profileService.updateMyProfile(user, request)).thenReturn(SOME_PROFILE);

        ProfileResponse response = controller.updateMyProfile("Bearer token", request);

        assertThat(response).isEqualTo(SOME_PROFILE);
        verify(profileService).updateMyProfile(user, request);
    }

    @Test
    void getMyProfilePropagatesUnauthorizedFromCurrentUserService() {
        when(currentUserService.requireCurrentUser(null))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Bearer token"));

        assertThatThrownBy(() -> controller.getMyProfile(null))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
        verifyNoInteractions(profileService);
    }

    @Test
    void getPublicProfileDoesNotRequireAuth() {
        when(profileService.getPublicProfile(7L)).thenReturn(SOME_PROFILE);

        ProfileResponse response = controller.getPublicProfile(7L);

        assertThat(response).isEqualTo(SOME_PROFILE);
        verifyNoInteractions(currentUserService);
        verify(profileService).getPublicProfile(7L);
    }
}
