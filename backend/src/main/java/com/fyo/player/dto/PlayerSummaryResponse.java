package com.fyo.player.dto;

/** One row of the player finder: a user paired with the sport (and skill
 *  level in it) that matched the search. */
public record PlayerSummaryResponse(
        Long id,
        String username,
        String name,
        String surname,
        String region,
        String imageUrl,
        Long sportId,
        String sportName,
        String skillLevel
) {
}