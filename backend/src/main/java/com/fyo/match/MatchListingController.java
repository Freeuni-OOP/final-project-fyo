package com.fyo.match;

import com.fyo.auth.CurrentUserService;
import com.fyo.domain.User;
import com.fyo.match.dto.AcceptListingResult;
import com.fyo.match.dto.CreateMatchListingRequest;
import com.fyo.match.dto.ListingResponseItem;
import com.fyo.match.dto.MatchListingItem;
import com.fyo.match.dto.RespondToListingRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Match listing lifecycle API. Browsing open listings is public; everything
 * that acts on a listing resolves the caller from the Bearer token.
 */
@RestController
@RequestMapping("/api/match-listings")
public class MatchListingController {

    private final MatchListingService listingService;
    private final CurrentUserService currentUserService;

    public MatchListingController(MatchListingService listingService, CurrentUserService currentUserService) {
        this.listingService = listingService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<MatchListingItem> getOpenListings(@RequestParam(required = false) Long sportId) {
        return listingService.getOpenListings(sportId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatchListingItem createListing(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody CreateMatchListingRequest request
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        return listingService.createListing(currentUser, request);
    }

    @PostMapping("/{listingId}/responses")
    @ResponseStatus(HttpStatus.CREATED)
    public ListingResponseItem respondToListing(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long listingId,
            @RequestBody(required = false) RespondToListingRequest request
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        RespondToListingRequest body = request == null ? new RespondToListingRequest(null) : request;
        return listingService.respondToListing(currentUser, listingId, body);
    }

    @GetMapping("/{listingId}/responses")
    public List<ListingResponseItem> getPendingResponses(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long listingId
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        return listingService.getPendingResponses(currentUser, listingId);
    }

    @PostMapping("/{listingId}/responses/{responseId}/accept")
    public AcceptListingResult acceptResponse(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long listingId,
            @PathVariable Long responseId
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        return listingService.acceptResponse(currentUser, listingId, responseId);
    }

    @PostMapping("/{listingId}/responses/{responseId}/decline")
    public ListingResponseItem declineResponse(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long listingId,
            @PathVariable Long responseId
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        return listingService.declineResponse(currentUser, listingId, responseId);
    }
}
