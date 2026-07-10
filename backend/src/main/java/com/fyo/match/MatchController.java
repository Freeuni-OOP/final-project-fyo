package com.fyo.match;

import com.fyo.auth.CurrentUserService;
import com.fyo.domain.User;
import com.fyo.match.dto.MatchResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;
    private final CurrentUserService currentUserService;

    public MatchController(MatchService matchService, CurrentUserService currentUserService) {
        this.matchService = matchService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<MatchResponse> getMatches(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long teamId
    ) {
        return matchService.getMatches(userId, teamId);
    }

    /** Matches the signed-in user plays in (1v1 or via team roster). */
    @GetMapping("/mine")
    public List<MatchResponse> getMyMatches(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        return matchService.getMatches(currentUser.getId(), null);
    }

    @GetMapping("/{id}")
    public MatchResponse getMatch(@PathVariable Long id) {
        return matchService.getMatch(id);
    }

    @PostMapping("/{id}/cancel")
    public MatchResponse cancelMatch(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        return matchService.cancelMatch(id, currentUser.getId());
    }
}
