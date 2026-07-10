package com.fyo.match;

import com.fyo.domain.MatchRequestStatus;
import com.fyo.match.dto.CreateMatchRequest;
import com.fyo.match.dto.MatchRequestResponse;
import com.fyo.match.dto.MatchResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping
    public List<MatchResponse> getMatches(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long teamId
    ) {
        return matchService.getMatches(userId, teamId);
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public MatchRequestResponse createMatchRequest(@Valid @RequestBody CreateMatchRequest request) {
        return matchService.createMatchRequest(request);
    }

    @GetMapping("/requests")
    public List<MatchRequestResponse> getMatchRequests(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) MatchRequestStatus status
    ) {
        return matchService.getMatchRequests(userId, teamId, status);
    }

    @PostMapping("/requests/{requestId}/accept")
    public MatchRequestResponse acceptMatchRequest(@PathVariable Long requestId, @RequestParam Long actingUserId) {
        return matchService.acceptMatchRequest(requestId, actingUserId);
    }

    @PostMapping("/requests/{requestId}/decline")
    public MatchRequestResponse declineMatchRequest(@PathVariable Long requestId, @RequestParam Long actingUserId) {
        return matchService.declineMatchRequest(requestId, actingUserId);
    }

    @GetMapping("/{id}")
    public MatchResponse getMatch(@PathVariable Long id) {
        return matchService.getMatch(id);
    }

    @PostMapping("/{id}/cancel")
    public MatchResponse cancelMatch(@PathVariable Long id, @RequestParam Long actingUserId) {
        return matchService.cancelMatch(id, actingUserId);
    }
}
