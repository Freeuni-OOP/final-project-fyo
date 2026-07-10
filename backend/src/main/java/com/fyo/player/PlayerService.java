package com.fyo.player;

import com.fyo.domain.SkillLevel;
import com.fyo.domain.UserSport;
import com.fyo.player.dto.PlayerSummaryResponse;
import com.fyo.repository.UserSportRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PlayerService {

    private static final int MAX_RESULTS = 100;

    private final UserSportRepository userSportRepository;

    public PlayerService(UserSportRepository userSportRepository) {
        this.userSportRepository = userSportRepository;
    }

    /** Every filter is optional; a null/blank value matches everything for that column. */
    @Transactional(readOnly = true)
    public List<PlayerSummaryResponse> search(Long sportId, String region, String skillLevel, int limit) {
        SkillLevel parsedSkill = parseSkillLevel(skillLevel);
        String trimmedRegion = (region == null || region.isBlank()) ? null : region.trim();
        int capped = Math.min(Math.max(limit, 1), MAX_RESULTS);

        return userSportRepository
                .search(sportId, trimmedRegion, parsedSkill, PageRequest.of(0, capped))
                .stream()
                .map(PlayerService::toSummary)
                .toList();
    }

    private static SkillLevel parseSkillLevel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return SkillLevel.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid skill level: " + value + ". Must be one of: BEGINNER, INTERMEDIATE, ADVANCED");
        }
    }

    private static PlayerSummaryResponse toSummary(UserSport userSport) {
        return new PlayerSummaryResponse(
                userSport.getUser().getId(),
                userSport.getUser().getUsername(),
                userSport.getUser().getName(),
                userSport.getUser().getSurname(),
                userSport.getUser().getRegion(),
                userSport.getUser().getImageUrl(),
                userSport.getSport().getId(),
                userSport.getSport().getSportName(),
                userSport.getSkillLevel().name()
        );
    }
}