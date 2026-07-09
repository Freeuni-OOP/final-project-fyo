package com.fyo.match;

import com.fyo.match.dto.MatchResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

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

    @GetMapping("/{id}")
    public MatchResponse getMatch(@PathVariable Long id) {
        return matchService.getMatch(id);
    }

    @PostMapping("/{id}/cancel")
    public MatchResponse cancelMatch(@PathVariable Long id, @RequestParam Long actingUserId) {
        return matchService.cancelMatch(id, actingUserId);
    }
}