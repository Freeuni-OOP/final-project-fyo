package com.fyo.profile;

import com.fyo.domain.Match;
import com.fyo.domain.MatchResult;
import com.fyo.domain.Sex;
import com.fyo.domain.SkillLevel;
import com.fyo.domain.Sport;
import com.fyo.domain.User;
import com.fyo.domain.UserReview;
import com.fyo.domain.UserSport;
import com.fyo.profile.dto.MatchHistoryItemResponse;
import com.fyo.profile.dto.ProfileResponse;
import com.fyo.profile.dto.ProfileSportResponse;
import com.fyo.profile.dto.ProfileSportUpdateDto;
import com.fyo.profile.dto.ReviewResponse;
import com.fyo.profile.dto.UpdateProfileRequest;
import com.fyo.repository.MatchRepository;
import com.fyo.repository.SportRepository;
import com.fyo.repository.UserRepository;
import com.fyo.repository.UserReviewRepository;
import com.fyo.repository.UserSportRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final UserSportRepository userSportRepository;
    private final SportRepository sportRepository;
    private final MatchRepository matchRepository;
    private final UserReviewRepository userReviewRepository;

    public ProfileService(
            UserRepository userRepository,
            UserSportRepository userSportRepository,
            SportRepository sportRepository,
            MatchRepository matchRepository,
            UserReviewRepository userReviewRepository
    ) {
        this.userRepository = userRepository;
        this.userSportRepository = userSportRepository;
        this.sportRepository = sportRepository;
        this.matchRepository = matchRepository;
        this.userReviewRepository = userReviewRepository;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getPublicProfile(Long userId) {
        return toProfileResponse(requireActiveUser(userId), false);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getMyProfile(User currentUser) {
        User user = requireActiveUser(currentUser.getId());
        return toProfileResponse(user, true);
    }

    @Transactional
    public ProfileResponse updateMyProfile(User currentUser, UpdateProfileRequest request) {
        User user = requireActiveUser(currentUser.getId());

        String requestedUsername = request.username().trim();
        if (!requestedUsername.equals(user.getUsername())
                && userRepository.existsByUsername(requestedUsername)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }

        user.setName(request.name().trim());
        user.setSurname(request.surname().trim());
        user.setUsername(requestedUsername);
        user.setAge(request.age().shortValue());
        user.setSex(parseSex(request.sex()));
        user.setRegion(request.region());
        user.setImageUrl(request.imageUrl());

        userSportRepository.deleteByUserId(user.getId());
        List<UserSport> newSports = request.sports().stream()
                .map(dto -> toUserSport(user, dto))
                .toList();
        userSportRepository.saveAll(newSports);
        userRepository.save(user);

        return toProfileResponse(user, true);
    }

    private User requireActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.isArchived()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return user;
    }

    private ProfileResponse toProfileResponse(User user, boolean includeEmail) {
        List<UserSport> sports = userSportRepository.findByUserId(user.getId());
        List<Match> matches = matchRepository.findHistoryForUser(user.getId());
        List<UserReview> reviews = userReviewRepository.findByReviewedUserIdOrderByCreatedAtDesc(user.getId());
        Double ratingAverage = userReviewRepository.findAverageScoreByReviewedUserId(user.getId())
                .orElse(null);

        return new ProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getSurname(),
                user.getAge(),
                user.getSex() != null ? user.getSex().name() : null,
                user.getRegion(),
                user.getImageUrl(),
                includeEmail ? user.getEmail() : null,
                ratingAverage,
                sports.stream().map(this::toSportResponse).toList(),
                matches.stream().map(match -> toMatchHistoryItem(match, user.getId())).toList(),
                reviews.stream().map(this::toReviewResponse).toList()
        );
    }

    private ProfileSportResponse toSportResponse(UserSport userSport) {
        return new ProfileSportResponse(
                userSport.getSport().getId(),
                userSport.getSport().getSportName(),
                userSport.getSkillLevel().name()
        );
    }

    private MatchHistoryItemResponse toMatchHistoryItem(Match match, Long profileUserId) {
        User opponent = null;
        if (match.getHomeUser() != null && match.getHomeUser().getId().equals(profileUserId)) {
            opponent = match.getAwayUser();
        } else if (match.getAwayUser() != null && match.getAwayUser().getId().equals(profileUserId)) {
            opponent = match.getHomeUser();
        }

        MatchResult result = match.getResult();
        return new MatchHistoryItemResponse(
                match.getId(),
                match.getSport().getSportName(),
                match.getFormat().name(),
                match.getStatus().name(),
                match.getLocation(),
                match.getProposedDatetime() != null ? match.getProposedDatetime().toInstant() : null,
                opponent != null ? opponent.getId() : null,
                opponent != null ? opponent.getUsername() : null,
                match.getHomeTeam() != null ? match.getHomeTeam().getId() : null,
                match.getHomeTeam() != null ? match.getHomeTeam().getName() : null,
                match.getAwayTeam() != null ? match.getAwayTeam().getId() : null,
                match.getAwayTeam() != null ? match.getAwayTeam().getName() : null,
                result != null ? result.getHomeScore() : null,
                result != null ? result.getAwayScore() : null,
                result != null ? result.getWinner().name() : null
        );
    }

    private ReviewResponse toReviewResponse(UserReview review) {
        return new ReviewResponse(
                review.getId(),
                review.getMatch().getId(),
                review.getReviewerUser().getId(),
                review.getReviewerUser().getUsername(),
                review.getScore(),
                review.getComment(),
                review.getCreatedAt()
        );
    }

    private UserSport toUserSport(User user, ProfileSportUpdateDto dto) {
        Sport sport = sportRepository.findById(dto.sportId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Sport not found: " + dto.sportId()));
        return new UserSport(user, sport, parseSkillLevel(dto.skillLevel()));
    }

    private SkillLevel parseSkillLevel(String value) {
        try {
            return SkillLevel.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid skill level: " + value
                    + ". Must be one of: BEGINNER, INTERMEDIATE, ADVANCED");
        }
    }

    private Sex parseSex(String value) {
        try {
            return Sex.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid sex value: " + value
                    + ". Must be one of: MALE, FEMALE, OTHER");
        }
    }
}
