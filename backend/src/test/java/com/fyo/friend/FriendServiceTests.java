package com.fyo.friend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fyo.domain.User;
import com.fyo.friend.dto.FriendRelationshipStatus;
import com.fyo.friend.dto.FriendRequestResponse;
import com.fyo.friend.dto.FriendStatusResponse;
import com.fyo.friend.dto.FriendSummaryResponse;
import com.fyo.friend.dto.SendFriendRequestBody;
import com.fyo.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@Transactional
class FriendServiceTests {

    @Autowired
    private FriendService friendService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void sendRequestCreatesPendingRequest() {
        List<User> users = userRepository.findAll();
        User requester = users.get(0);
        User addressee = users.get(1);

        FriendRequestResponse response = friendService.sendRequest(
                requester.getId(),
                new SendFriendRequestBody(addressee.getId())
        );

        assertThat(response.status().name()).isEqualTo("PENDING");
        assertThat(response.requester().id()).isEqualTo(requester.getId());
        assertThat(response.addressee().id()).isEqualTo(addressee.getId());
    }

    @Test
    void sendRequestRejectsSelf() {
        User user = userRepository.findAll().get(0);

        assertThatThrownBy(() -> friendService.sendRequest(
                user.getId(),
                new SendFriendRequestBody(user.getId())
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("cannot add yourself");
    }

    @Test
    void sendRequestRejectsDuplicatePending() {
        List<User> users = userRepository.findAll();
        User requester = users.get(0);
        User addressee = users.get(1);

        friendService.sendRequest(requester.getId(), new SendFriendRequestBody(addressee.getId()));

        assertThatThrownBy(() -> friendService.sendRequest(
                requester.getId(),
                new SendFriendRequestBody(addressee.getId())
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already sent");
    }

    @Test
    void sendRequestRejectsReversePending() {
        List<User> users = userRepository.findAll();
        User userA = users.get(0);
        User userB = users.get(1);

        friendService.sendRequest(userA.getId(), new SendFriendRequestBody(userB.getId()));

        assertThatThrownBy(() -> friendService.sendRequest(
                userB.getId(),
                new SendFriendRequestBody(userA.getId())
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already sent you a friend request");
    }

    @Test
    void acceptRequestCreatesFriendship() {
        List<User> users = userRepository.findAll();
        User requester = users.get(0);
        User addressee = users.get(1);

        FriendRequestResponse pending = friendService.sendRequest(
                requester.getId(),
                new SendFriendRequestBody(addressee.getId())
        );

        FriendRequestResponse accepted = friendService.acceptRequest(addressee.getId(), pending.id());

        assertThat(accepted.status().name()).isEqualTo("ACCEPTED");
        assertThat(friendService.getFriends(requester.getId())).hasSize(1);
        assertThat(friendService.getFriends(addressee.getId())).hasSize(1);
    }

    @Test
    void acceptRequestOnlyByAddressee() {
        List<User> users = userRepository.findAll();
        User requester = users.get(0);
        User addressee = users.get(1);
        User outsider = users.get(2);

        FriendRequestResponse pending = friendService.sendRequest(
                requester.getId(),
                new SendFriendRequestBody(addressee.getId())
        );

        assertThatThrownBy(() -> friendService.acceptRequest(outsider.getId(), pending.id()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only the recipient");
    }

    @Test
    void declineRequestLeavesUsersUnfriended() {
        List<User> users = userRepository.findAll();
        User requester = users.get(0);
        User addressee = users.get(1);

        FriendRequestResponse pending = friendService.sendRequest(
                requester.getId(),
                new SendFriendRequestBody(addressee.getId())
        );

        FriendRequestResponse declined = friendService.declineRequest(addressee.getId(), pending.id());

        assertThat(declined.status().name()).isEqualTo("DECLINED");
        assertThat(friendService.getFriends(requester.getId())).isEmpty();
        FriendStatusResponse status = friendService.getRelationshipStatus(requester.getId(), addressee.getId());
        assertThat(status.status()).isEqualTo(FriendRelationshipStatus.NONE);
    }

    @Test
    void cancelOutgoingRequestRemovesPendingRow() {
        List<User> users = userRepository.findAll();
        User requester = users.get(0);
        User addressee = users.get(1);

        FriendRequestResponse pending = friendService.sendRequest(
                requester.getId(),
                new SendFriendRequestBody(addressee.getId())
        );

        friendService.cancelRequest(requester.getId(), pending.id());

        assertThat(friendService.getOutgoingRequests(requester.getId())).isEmpty();
    }

    @Test
    void unfriendRemovesAcceptedRelationship() {
        List<User> users = userRepository.findAll();
        User requester = users.get(0);
        User addressee = users.get(1);

        FriendRequestResponse pending = friendService.sendRequest(
                requester.getId(),
                new SendFriendRequestBody(addressee.getId())
        );
        friendService.acceptRequest(addressee.getId(), pending.id());

        friendService.unfriend(requester.getId(), addressee.getId());

        assertThat(friendService.getFriends(requester.getId())).isEmpty();
        assertThat(friendService.getRelationshipStatus(requester.getId(), addressee.getId()).status())
                .isEqualTo(FriendRelationshipStatus.NONE);
    }

    @Test
    void getRelationshipStatusReportsIncomingPending() {
        List<User> users = userRepository.findAll();
        User requester = users.get(0);
        User addressee = users.get(1);

        FriendRequestResponse pending = friendService.sendRequest(
                requester.getId(),
                new SendFriendRequestBody(addressee.getId())
        );

        FriendStatusResponse status = friendService.getRelationshipStatus(addressee.getId(), requester.getId());

        assertThat(status.status()).isEqualTo(FriendRelationshipStatus.PENDING_INCOMING);
        assertThat(status.requestId()).isEqualTo(pending.id());
    }

    @Test
    void listFriendsReturnsOtherUserSummary() {
        List<User> users = userRepository.findAll();
        User requester = users.get(0);
        User addressee = users.get(1);

        FriendRequestResponse pending = friendService.sendRequest(
                requester.getId(),
                new SendFriendRequestBody(addressee.getId())
        );
        friendService.acceptRequest(addressee.getId(), pending.id());

        List<FriendSummaryResponse> friends = friendService.getFriends(requester.getId());

        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).user().id()).isEqualTo(addressee.getId());
    }
}
