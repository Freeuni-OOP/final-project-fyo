package com.fyo.player;

import com.fyo.player.dto.PlayerSummaryResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    /** Player finder: filter by sport, region, and skill level. All params optional. */
    @GetMapping
    public List<PlayerSummaryResponse> search(
            @RequestParam(required = false) Long sportId,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String skillLevel,
            @RequestParam(defaultValue = "60") int limit
    ) {
        return playerService.search(sportId, region, skillLevel, limit);
    }
}