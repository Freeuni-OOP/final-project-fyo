package com.fyo.onboarding;

import com.fyo.domain.SkillLevel;
import com.fyo.domain.Sport;
import com.fyo.domain.User;
import com.fyo.domain.UserSport;
import com.fyo.onboarding.dto.OnboardingRequest;
import com.fyo.onboarding.dto.OnboardingResponse;
import com.fyo.onboarding.dto.OnboardingStatusResponse;
import com.fyo.onboarding.dto.UserSportDto;
import com.fyo.onboarding.dto.UserSportResponse;
import com.fyo.repository.SportRepository;
import com.fyo.repository.UserRepository;
import com.fyo.repository.UserSportRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OnboardingService {

    private final UserRepository userRepository;
    private final SportRepository sportRepository;
    private final UserSportRepository userSportRepository;

    public OnboardingService(
            UserRepository userRepository,
            SportRepository sportRepository,
            UserSportRepository userSportRepository
    ) {
        this.userRepository = userRepository;
        this.sportRepository = sportRepository;
        this.userSportRepository = userSportRepository;
    }

    @Transactional
    public OnboardingResponse completeOnboarding(Long userId, OnboardingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.isOnboarding()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Onboarding already completed");
        }

        String requestedUsername = request.username().trim();
        if (userRepository.existsByUsername(requestedUsername)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }

        SkillLevel skillLevel;
        Sex sex;

        user.setName(request.name().trim());
        user.setSurname(request.surname().trim());
        user.setUsername(requestedUsername);
        user.setAge(request.age().shortValue());
        user.setSex(parseSex(request.sex()));
        user.setRegion(request.region());
        user.setImageUrl(request.imageUrl());
        user.setOnboarding(true);

        userSportRepository.deleteByUserId(userId);

        List<UserSport> newSports = request.sports().stream()
                .map(dto -> toUserSport(user, dto))
                .toList();
        userSportRepository.saveAll(newSports);

        userRepository.save(user);

        return toOnboardingResponse(user, newSports);
    }

    @Transactional(readOnly = true)
    public OnboardingStatusResponse getOnboardingStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return new OnboardingStatusResponse(user.isOnboarding());
    }

    private UserSport toUserSport(User user, UserSportDto dto) {
        Sport sport = sportRepository.findById(dto.sportId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Sport not found: " + dto.sportId()));
        SkillLevel skillLevel = parseSkillLevel(dto.skillLevel());
        return new UserSport(user, sport, skillLevel);
    }

    private SkillLevel parseSkillLevel(String value) {
        try {
            return SkillLevel.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid skill level: " + value +
                    ". Must be one of: BEGINNER, INTERMEDIATE, ADVANCED");
        }
    }

    private com.fyo.domain.Sex parseSex(String value) {
        try {
            return com.fyo.domain.Sex.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid sex value: " + value +
                    ". Must be one of: MALE, FEMALE, OTHER");
        }
    }

    private OnboardingResponse toOnboardingResponse(User user, List<UserSport> sports) {
        return new OnboardingResponse(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getUsername(),
                user.getAge(),
                user.getSex().name(),
                user.getRegion(),
                user.getImageUrl(),
                user.isOnboarding(),
                sports.stream().map(this::toUserSportResponse).toList()
        );
    }

    private UserSportResponse toUserSportResponse(UserSport userSport) {
        return new UserSportResponse(
                userSport.getSport().getId(),
                userSport.getSport().getSportName(),
                userSport.getSkillLevel().name()
        );
    }
}