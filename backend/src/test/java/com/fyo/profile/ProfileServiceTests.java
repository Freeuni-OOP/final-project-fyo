package com.fyo.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fyo.domain.Sex;
import com.fyo.domain.SkillLevel;
import com.fyo.domain.Sport;
import com.fyo.domain.User;
import com.fyo.domain.UserSport;
import com.fyo.profile.dto.ProfileResponse;
import com.fyo.profile.dto.ProfileSportUpdateDto;
import com.fyo.profile.dto.UpdateProfileRequest;
import com.fyo.repository.MatchRepository;
import com.fyo.repository.SportRepository;
import com.fyo.repository.UserRepository;
import com.fyo.repository.UserReviewRepository;
import com.fyo.repository.UserSportRepository;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class ProfileServiceTests {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserSportRepository userSportRepository = mock(UserSportRepository.class);
    private final SportRepository sportRepository = mock(SportRepository.class);
    private final MatchRepository matchRepository = mock(MatchRepository.class);
    private final UserReviewRepository userReviewRepository = mock(UserReviewRepository.class);

    private final ProfileService profileService = new ProfileService(
            userRepository,
            userSportRepository,
            sportRepository,
            matchRepository,
            userReviewRepository
    );

    private User user;
    private Sport football;

    @BeforeEach
    void setUp() throws Exception {
        user = new User("uid-1", "Ana", "Kobalia", "ana", "ana@example.com");
        setId(user, 1L);
        user.setAge((short) 22);
        user.setSex(Sex.FEMALE);
        user.setRegion("Tbilisi");
        user.setImageUrl("https://example.com/a.png");
        user.setOnboarding(false);

        football = new Sport("Football");
        setId(football, 10L);
    }

    @Test
    void getPublicProfileReturnsSportsMatchesReviewsAndAverageWithoutEmail() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userSportRepository.findByUserId(1L))
                .thenReturn(List.of(new UserSport(user, football, SkillLevel.ADVANCED)));
        when(matchRepository.findHistoryForUser(1L)).thenReturn(List.of());
        when(userReviewRepository.findByReviewedUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        when(userReviewRepository.findAverageScoreByReviewedUserId(1L)).thenReturn(Optional.of(4.5));

        ProfileResponse response = profileService.getPublicProfile(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("ana");
        assertThat(response.email()).isNull();
        assertThat(response.ratingAverage()).isEqualTo(4.5);
        assertThat(response.sports()).hasSize(1);
        assertThat(response.sports().getFirst().sportName()).isEqualTo("Football");
        assertThat(response.sports().getFirst().skillLevel()).isEqualTo("ADVANCED");
        assertThat(response.matchHistory()).isEmpty();
        assertThat(response.reviews()).isEmpty();
    }

    @Test
    void getMyProfileIncludesEmail() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userSportRepository.findByUserId(1L)).thenReturn(List.of());
        when(matchRepository.findHistoryForUser(1L)).thenReturn(List.of());
        when(userReviewRepository.findByReviewedUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        when(userReviewRepository.findAverageScoreByReviewedUserId(1L)).thenReturn(Optional.empty());

        ProfileResponse response = profileService.getMyProfile(user);

        assertThat(response.email()).isEqualTo("ana@example.com");
        assertThat(response.ratingAverage()).isNull();
    }

    @Test
    void getPublicProfileReturnsNotFoundForMissingUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getPublicProfile(99L))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void getPublicProfileReturnsNotFoundForArchivedUser() {
        user.setArchived(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> profileService.getPublicProfile(1L))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void updateMyProfileUpdatesFieldsAndReplacesSports() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("ana_new")).thenReturn(false);
        when(sportRepository.findById(10L)).thenReturn(Optional.of(football));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userSportRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(userSportRepository.findByUserId(1L))
                .thenReturn(List.of(new UserSport(user, football, SkillLevel.INTERMEDIATE)));
        when(matchRepository.findHistoryForUser(1L)).thenReturn(List.of());
        when(userReviewRepository.findByReviewedUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        when(userReviewRepository.findAverageScoreByReviewedUserId(1L)).thenReturn(Optional.empty());

        UpdateProfileRequest request = new UpdateProfileRequest(
                "Ana Updated",
                "Kobalia",
                "ana_new",
                23,
                "FEMALE",
                "Batumi",
                "https://example.com/new.png",
                List.of(new ProfileSportUpdateDto(10L, "INTERMEDIATE"))
        );

        ProfileResponse response = profileService.updateMyProfile(user, request);

        assertThat(response.name()).isEqualTo("Ana Updated");
        assertThat(response.username()).isEqualTo("ana_new");
        assertThat(response.age()).isEqualTo((short) 23);
        assertThat(response.region()).isEqualTo("Batumi");
        assertThat(response.email()).isEqualTo("ana@example.com");
        assertThat(response.sports()).hasSize(1);
        assertThat(response.sports().getFirst().skillLevel()).isEqualTo("INTERMEDIATE");

        verify(userSportRepository).deleteByUserId(1L);
        verify(userSportRepository).flush();
        verify(userSportRepository).saveAll(anyList());
        verify(userRepository).save(user);
    }

    @Test
    void updateMyProfileRejectsTakenUsername() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        UpdateProfileRequest request = new UpdateProfileRequest(
                "Ana", "Kobalia", "taken", 22, "FEMALE", "Tbilisi", null,
                List.of(new ProfileSportUpdateDto(10L, "BEGINNER"))
        );

        assertThatThrownBy(() -> profileService.updateMyProfile(user, request))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT))
                .hasMessageContaining("Username already taken");

        verify(userSportRepository, never()).deleteByUserId(any());
    }

    @Test
    void updateMyProfileRejectsInvalidSex() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UpdateProfileRequest request = new UpdateProfileRequest(
                "Ana", "Kobalia", "ana", 22, "UNKNOWN", "Tbilisi", null,
                List.of(new ProfileSportUpdateDto(10L, "BEGINNER"))
        );

        assertThatThrownBy(() -> profileService.updateMyProfile(user, request))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST))
                .hasMessageContaining("Invalid sex value");
    }

    @Test
    void updateMyProfileRejectsInvalidSkillLevel() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(sportRepository.findById(10L)).thenReturn(Optional.of(football));

        UpdateProfileRequest request = new UpdateProfileRequest(
                "Ana", "Kobalia", "ana", 22, "FEMALE", "Tbilisi", null,
                List.of(new ProfileSportUpdateDto(10L, "PRO"))
        );

        assertThatThrownBy(() -> profileService.updateMyProfile(user, request))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST))
                .hasMessageContaining("Invalid skill level");
    }

    @Test
    void updateMyProfileRejectsMissingSport() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(sportRepository.findById(999L)).thenReturn(Optional.empty());

        UpdateProfileRequest request = new UpdateProfileRequest(
                "Ana", "Kobalia", "ana", 22, "FEMALE", "Tbilisi", null,
                List.of(new ProfileSportUpdateDto(999L, "BEGINNER"))
        );

        assertThatThrownBy(() -> profileService.updateMyProfile(user, request))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND))
                .hasMessageContaining("Sport not found");
    }

    private static void setId(Object entity, Long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }
}
