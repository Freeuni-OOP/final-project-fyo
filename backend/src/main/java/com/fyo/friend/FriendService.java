package com.fyo.friend;

import com.fyo.domain.FriendRequest;
import com.fyo.domain.FriendRequestStatus;
import com.fyo.domain.User;
import com.fyo.friend.dto.FriendRelationshipStatus;
import com.fyo.friend.dto.FriendRequestResponse;
import com.fyo.friend.dto.FriendStatusResponse;
import com.fyo.friend.dto.FriendSummaryResponse;
import com.fyo.friend.dto.SendFriendRequestBody;
import com.fyo.notification.NotificationService;
import com.fyo.repository.FriendRequestRepository;
import com.fyo.repository.UserRepository;
import com.fyo.team.dto.UserSummaryResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FriendService {

    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public FriendService(
            FriendRequestRepository friendRequestRepository,
            UserRepository userRepository,
            NotificationService notificationService
    ) {
        this.friendRequestRepository = friendRequestRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<FriendSummaryResponse> getFriends(Long currentUserId) {
        ensureUserExists(currentUserId);
        return friendRequestRepository.findAcceptedForUser(currentUserId).stream()
                .map(request -> toFriendSummary(request, currentUserId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FriendRequestResponse> getIncomingRequests(Long currentUserId) {
        ensureUserExists(currentUserId);
        return friendRequestRepository
                .findByAddresseeIdAndStatusOrderByCreatedAtDesc(currentUserId, FriendRequestStatus.PENDING)
                .stream()
                .map(this::toFriendRequestResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FriendRequestResponse> getOutgoingRequests(Long currentUserId) {
        ensureUserExists(currentUserId);
        return friendRequestRepository
                .findByRequesterIdAndStatusOrderByCreatedAtDesc(currentUserId, FriendRequestStatus.PENDING)
                .stream()
                .map(this::toFriendRequestResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FriendStatusResponse getRelationshipStatus(Long currentUserId, Long otherUserId) {
        ensureUserExists(currentUserId);
        if (currentUserId.equals(otherUserId)) {
            return new FriendStatusResponse(otherUserId, FriendRelationshipStatus.NONE, null);
        }

        List<FriendRequest> between = friendRequestRepository.findBetweenUsers(currentUserId, otherUserId);
        if (between.isEmpty()) {
            return new FriendStatusResponse(otherUserId, FriendRelationshipStatus.NONE, null);
        }

        FriendRequest accepted = between.stream()
                .filter(request -> request.getStatus() == FriendRequestStatus.ACCEPTED)
                .findFirst()
                .orElse(null);
        if (accepted != null) {
            return new FriendStatusResponse(otherUserId, FriendRelationshipStatus.FRIENDS, accepted.getId());
        }

        FriendRequest outgoing = between.stream()
                .filter(request -> request.getRequester().getId().equals(currentUserId)
                        && request.getStatus() == FriendRequestStatus.PENDING)
                .findFirst()
                .orElse(null);
        if (outgoing != null) {
            return new FriendStatusResponse(otherUserId, FriendRelationshipStatus.PENDING_OUTGOING, outgoing.getId());
        }

        FriendRequest incoming = between.stream()
                .filter(request -> request.getAddressee().getId().equals(currentUserId)
                        && request.getStatus() == FriendRequestStatus.PENDING)
                .findFirst()
                .orElse(null);
        if (incoming != null) {
            return new FriendStatusResponse(otherUserId, FriendRelationshipStatus.PENDING_INCOMING, incoming.getId());
        }

        return new FriendStatusResponse(otherUserId, FriendRelationshipStatus.NONE, null);
    }

    @Transactional
    public FriendRequestResponse sendRequest(Long currentUserId, SendFriendRequestBody body) {
        if (currentUserId.equals(body.addresseeUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot add yourself as a friend");
        }

        User requester = ensureActiveUser(currentUserId);
        User addressee = ensureActiveUser(body.addresseeUserId());

        List<FriendRequest> between = friendRequestRepository.findBetweenUsers(currentUserId, body.addresseeUserId());

        FriendRequest accepted = between.stream()
                .filter(request -> request.getStatus() == FriendRequestStatus.ACCEPTED)
                .findFirst()
                .orElse(null);
        if (accepted != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You are already friends");
        }

        FriendRequest reversePending = between.stream()
                .filter(request -> request.getRequester().getId().equals(body.addresseeUserId())
                        && request.getStatus() == FriendRequestStatus.PENDING)
                .findFirst()
                .orElse(null);
        if (reversePending != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This user already sent you a friend request - accept it instead"
            );
        }

        FriendRequest existingOutgoing = friendRequestRepository
                .findByRequesterIdAndAddresseeId(currentUserId, body.addresseeUserId())
                .orElse(null);
        if (existingOutgoing != null) {
            if (existingOutgoing.getStatus() == FriendRequestStatus.PENDING) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Friend request already sent");
            }
            if (existingOutgoing.getStatus() == FriendRequestStatus.DECLINED) {
                existingOutgoing.reopen();
                FriendRequest saved = friendRequestRepository.save(existingOutgoing);
                notifyFriendRequest(saved);
                return toFriendRequestResponse(saved);
            }
        }

        FriendRequest saved = friendRequestRepository.save(new FriendRequest(requester, addressee));
        notifyFriendRequest(saved);
        return toFriendRequestResponse(saved);
    }

    @Transactional
    public FriendRequestResponse acceptRequest(Long currentUserId, Long requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend request not found"));

        if (!request.getAddressee().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the recipient can accept this request");
        }
        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Friend request is no longer pending");
        }

        request.accept();
        FriendRequest saved = friendRequestRepository.save(request);
        notificationService.notifyUser(
                saved.getRequester().getId(),
                "FRIEND_ACCEPTED",
                saved.getAddressee().getUsername() + " accepted your friend request",
                "#/app/friends"
        );
        return toFriendRequestResponse(saved);
    }

    @Transactional
    public FriendRequestResponse declineRequest(Long currentUserId, Long requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend request not found"));

        if (!request.getAddressee().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the recipient can decline this request");
        }
        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Friend request is no longer pending");
        }

        request.decline();
        return toFriendRequestResponse(friendRequestRepository.save(request));
    }

    @Transactional
    public void cancelRequest(Long currentUserId, Long requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend request not found"));

        if (!request.getRequester().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the sender can cancel this request");
        }
        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Friend request is no longer pending");
        }

        friendRequestRepository.delete(request);
    }

    @Transactional
    public void unfriend(Long currentUserId, Long friendUserId) {
        if (currentUserId.equals(friendUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid friend user");
        }

        FriendRequest friendship = friendRequestRepository.findBetweenUsers(currentUserId, friendUserId).stream()
                .filter(request -> request.getStatus() == FriendRequestStatus.ACCEPTED)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friendship not found"));

        if (!friendship.getRequester().getId().equals(currentUserId)
                && !friendship.getAddressee().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not part of this friendship");
        }

        friendRequestRepository.delete(friendship);
    }


    private void notifyFriendRequest(FriendRequest request) {
        notificationService.notifyUser(
                request.getAddressee().getId(),
                "FRIEND_REQUEST",
                request.getRequester().getUsername() + " sent you a friend request",
                "#/app/friends"
        );
    }

    private User ensureUserExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private User ensureActiveUser(Long userId) {
        User user = ensureUserExists(userId);
        if (user.isArchived()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User account is not available");
        }
        return user;
    }

    private FriendSummaryResponse toFriendSummary(FriendRequest request, Long currentUserId) {
        User friend = request.getRequester().getId().equals(currentUserId)
                ? request.getAddressee()
                : request.getRequester();
        return new FriendSummaryResponse(
                request.getId(),
                toUserSummary(friend),
                request.getUpdatedAt() != null ? request.getUpdatedAt() : request.getCreatedAt()
        );
    }

    private FriendRequestResponse toFriendRequestResponse(FriendRequest request) {
        return new FriendRequestResponse(
                request.getId(),
                toUserSummary(request.getRequester()),
                toUserSummary(request.getAddressee()),
                request.getStatus(),
                request.getCreatedAt()
        );
    }

    private UserSummaryResponse toUserSummary(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getSurname(),
                user.getRegion(),
                user.getImageUrl()
        );
    }
}
