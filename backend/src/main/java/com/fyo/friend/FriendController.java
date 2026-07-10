package com.fyo.friend;

import com.fyo.auth.CurrentUserService;
import com.fyo.domain.User;
import com.fyo.friend.dto.FriendRequestResponse;
import com.fyo.friend.dto.FriendStatusResponse;
import com.fyo.friend.dto.FriendSummaryResponse;
import com.fyo.friend.dto.SendFriendRequestBody;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendService friendService;
    private final CurrentUserService currentUserService;

    public FriendController(FriendService friendService, CurrentUserService currentUserService) {
        this.friendService = friendService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<FriendSummaryResponse> getFriends(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        return friendService.getFriends(currentUser.getId());
    }

    @GetMapping("/requests/incoming")
    public List<FriendRequestResponse> getIncomingRequests(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        return friendService.getIncomingRequests(currentUser.getId());
    }

    @GetMapping("/requests/outgoing")
    public List<FriendRequestResponse> getOutgoingRequests(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        return friendService.getOutgoingRequests(currentUser.getId());
    }

    @GetMapping("/status/{userId}")
    public FriendStatusResponse getStatus(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long userId
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        return friendService.getRelationshipStatus(currentUser.getId(), userId);
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public FriendRequestResponse sendRequest(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody SendFriendRequestBody body
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        return friendService.sendRequest(currentUser.getId(), body);
    }

    @PostMapping("/requests/{requestId}/accept")
    public FriendRequestResponse acceptRequest(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long requestId
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        return friendService.acceptRequest(currentUser.getId(), requestId);
    }

    @PostMapping("/requests/{requestId}/decline")
    public FriendRequestResponse declineRequest(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long requestId
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        return friendService.declineRequest(currentUser.getId(), requestId);
    }

    @DeleteMapping("/requests/{requestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelRequest(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long requestId
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        friendService.cancelRequest(currentUser.getId(), requestId);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfriend(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long userId
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        friendService.unfriend(currentUser.getId(), userId);
    }
}
