package com.fyo.friend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fyo.auth.CurrentUserService;
import com.fyo.domain.FriendRequestStatus;
import com.fyo.domain.User;
import com.fyo.friend.dto.FriendRequestResponse;
import com.fyo.friend.dto.SendFriendRequestBody;
import com.fyo.team.dto.UserSummaryResponse;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class FriendControllerTests {

    private final FriendService friendService = mock(FriendService.class);
    private final CurrentUserService currentUserService = mock(CurrentUserService.class);
    private final FriendController controller = new FriendController(friendService, currentUserService);

    private static final User CURRENT = new User("uid-1", "Ana", "Kobalia", "ana", "ana@example.com");
    private static final FriendRequestResponse SAMPLE_REQUEST = new FriendRequestResponse(
            5L,
            new UserSummaryResponse(1L, "ana", "Ana", "Kobalia", "Tbilisi", null),
            new UserSummaryResponse(2L, "niko", "Niko", "K", "Tbilisi", null),
            FriendRequestStatus.PENDING,
            OffsetDateTime.now()
    );

    @Test
    void sendRequestUsesAuthenticatedUser() {
        when(currentUserService.requireCurrentUser("Bearer token")).thenReturn(CURRENT);
        when(friendService.sendRequest(any(), eq(new SendFriendRequestBody(2L)))).thenReturn(SAMPLE_REQUEST);

        FriendRequestResponse response = controller.sendRequest("Bearer token", new SendFriendRequestBody(2L));

        assertThat(response).isEqualTo(SAMPLE_REQUEST);
        verify(friendService).sendRequest(any(), eq(new SendFriendRequestBody(2L)));
    }

    @Test
    void acceptRequestUsesAuthenticatedUser() {
        when(currentUserService.requireCurrentUser("Bearer token")).thenReturn(CURRENT);
        when(friendService.acceptRequest(any(), eq(5L))).thenReturn(SAMPLE_REQUEST);

        FriendRequestResponse response = controller.acceptRequest("Bearer token", 5L);

        assertThat(response).isEqualTo(SAMPLE_REQUEST);
        verify(friendService).acceptRequest(any(), eq(5L));
    }

    @Test
    void sendRequestPropagatesUnauthorized() {
        when(currentUserService.requireCurrentUser(null))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Bearer token"));

        assertThatThrownBy(() -> controller.sendRequest(null, new SendFriendRequestBody(2L)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
        verifyNoInteractions(friendService);
    }
}
