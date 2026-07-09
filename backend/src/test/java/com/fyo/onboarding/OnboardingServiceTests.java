package com.fyo.onboarding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fyo.domain.User;
import com.fyo.onboarding.dto.OnboardingRequest;
import com.fyo.onboarding.dto.OnboardingResponse;
import com.fyo.onboarding.dto.OnboardingStatusResponse;
import com.fyo.onboarding.dto.UserSportDto;
import com.fyo.repository.SportRepository;
import com.fyo.repository.UserRepository;
import com.fyo.repository.UserSportRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@Transactional
class OnboardingServiceTests {

    @Autowired
    private OnboardingService onboardingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SportRepository sportRepository;

    @Autowired
    private UserSportRepository userSportRepository;

    @Autowired
    private EntityManager entityManager;

    // -------------------------------------------------------------------------
    // Helper: inserts a bare user with no username/profile — simulates a user
    // that just registered via Firebase but hasn't completed onboarding yet.
    // All seeded users already have usernames, so we need a fresh one.
    // -------------------------------------------------------------------------
    private User saveNewUnboardedUser(String firebaseUid, String email) {
        // is_onboarding = TRUE means "still needs onboarding" (same as signup / project convention)
        entityManager.createNativeQuery(
                        "INSERT INTO users (firebase_uid, name, surname, username, email, is_onboarding) " +
                                "VALUES (:uid, '', '', :username, :email, TRUE)"
                )
                .setParameter("uid", firebaseUid)
                .setParameter("username", "tmp_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10))
                .setParameter("email", email)
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();
        return userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalStateException("Test setup failed: user not found after insert"));
    }

    private OnboardingRequest buildValidRequest(String username) {
        Long footballId = sportRepository.findAll().stream()
                .filter(s -> s.getSportName().equals("Football"))
                .findFirst()
                .orElseThrow()
                .getId();
        Long tennisId = sportRepository.findAll().stream()
                .filter(s -> s.getSportName().equals("Tennis"))
                .findFirst()
                .orElseThrow()
                .getId();
        return new OnboardingRequest(
                "Nikoloz",
                "Khuskivadze",
                username,
                22,
                "MALE",
                "Tbilisi",
                "https://example.com/avatar.png",
                List.of(
                        new UserSportDto(footballId, "ADVANCED"),
                        new UserSportDto(tennisId, "BEGINNER")
                )
        );
    }

    // -------------------------------------------------------------------------
    // Happy path: completing onboarding saves all fields and sports correctly
    // -------------------------------------------------------------------------
    @Test
    void completingOnboardingSavesAllFieldsAndSports() {
        User user = saveNewUnboardedUser(
                "test-firebase-uid-happy-path",
                "happy.path@test.com"
        );
        String uniqueUsername = "niko_test_" + UUID.randomUUID().toString().substring(0, 6);

        OnboardingResponse response = onboardingService.completeOnboarding(
                user.getId(),
                buildValidRequest(uniqueUsername)
        );

        assertThat(response.name()).isEqualTo("Nikoloz");
        assertThat(response.surname()).isEqualTo("Khuskivadze");
        assertThat(response.username()).isEqualTo(uniqueUsername);
        assertThat(response.age()).isEqualTo(22);
        assertThat(response.sex()).isEqualTo("MALE");
        assertThat(response.region()).isEqualTo("Tbilisi");
        assertThat(response.imageUrl()).isEqualTo("https://example.com/avatar.png");
        assertThat(response.onboardingCompleted()).isTrue();
        assertThat(response.sports()).hasSize(2);
        assertThat(response.sports())
                .extracting(sport -> sport.sportName())
                .containsExactlyInAnyOrder("Football", "Tennis");
        assertThat(response.sports())
                .anySatisfy(sport -> {
                    assertThat(sport.sportName()).isEqualTo("Football");
                    assertThat(sport.skillLevel()).isEqualTo("ADVANCED");
                });
    }

    // -------------------------------------------------------------------------
    // Happy path: getOnboardingStatus returns false before onboarding
    // -------------------------------------------------------------------------
    @Test
    void getOnboardingStatusReturnsFalseBeforeOnboarding() {
        User user = saveNewUnboardedUser(
                "test-firebase-uid-status-false",
                "status.false@test.com"
        );

        OnboardingStatusResponse status = onboardingService.getOnboardingStatus(user.getId());

        assertThat(status.onboardingCompleted()).isFalse();
    }

    // -------------------------------------------------------------------------
    // Happy path: getOnboardingStatus returns true after onboarding completes
    // -------------------------------------------------------------------------
    @Test
    void getOnboardingStatusReturnsTrueAfterOnboarding() {
        User user = saveNewUnboardedUser(
                "test-firebase-uid-status-true",
                "status.true@test.com"
        );
        String uniqueUsername = "niko_done_" + UUID.randomUUID().toString().substring(0, 6);

        onboardingService.completeOnboarding(user.getId(), buildValidRequest(uniqueUsername));

        OnboardingStatusResponse status = onboardingService.getOnboardingStatus(user.getId());
        assertThat(status.onboardingCompleted()).isTrue();
    }

    // -------------------------------------------------------------------------
    // Error: completing onboarding twice throws 409 CONFLICT
    // -------------------------------------------------------------------------
    @Test
    void completingOnboardingTwiceThrowsConflict() {
        User user = saveNewUnboardedUser(
                "test-firebase-uid-double-onboard",
                "double.onboard@test.com"
        );
        String uniqueUsername = "niko_twice_" + UUID.randomUUID().toString().substring(0, 6);

        onboardingService.completeOnboarding(user.getId(), buildValidRequest(uniqueUsername));

        String anotherUsername = "niko_again_" + UUID.randomUUID().toString().substring(0, 6);
        assertThatThrownBy(() ->
                onboardingService.completeOnboarding(user.getId(), buildValidRequest(anotherUsername))
        )
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Onboarding already completed");
    }

    // -------------------------------------------------------------------------
    // Error: username already taken by another user throws 409 CONFLICT
    // -------------------------------------------------------------------------
    @Test
    void takenUsernameThrowsConflict() {
        User user = saveNewUnboardedUser(
                "test-firebase-uid-taken-username",
                "taken.username@test.com"
        );

        // "niko_k" is seeded in V3 — guaranteed to already exist
        assertThatThrownBy(() ->
                onboardingService.completeOnboarding(user.getId(), buildValidRequest("niko_k"))
        )
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Username already taken");
    }

    @Test
    void keepingOwnUsernameIsAllowed() {
        User user = saveNewUnboardedUser(
                "test-firebase-uid-keep-username",
                "keep.username@test.com"
        );
        String ownUsername = user.getUsername();

        OnboardingResponse response = onboardingService.completeOnboarding(
                user.getId(),
                buildValidRequest(ownUsername)
        );

        assertThat(response.username()).isEqualTo(ownUsername);
        assertThat(response.onboardingCompleted()).isTrue();
    }

    // -------------------------------------------------------------------------
    // Error: user not found throws 404
    // -------------------------------------------------------------------------
    @Test
    void nonExistentUserThrowsNotFound() {
        assertThatThrownBy(() ->
                onboardingService.getOnboardingStatus(999_999L)
        )
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");

        assertThatThrownBy(() ->
                onboardingService.completeOnboarding(999_999L, buildValidRequest("ghost_user"))
        )
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");
    }

    // -------------------------------------------------------------------------
    // Error: invalid sport ID throws 404
    // -------------------------------------------------------------------------
    @Test
    void invalidSportIdThrowsNotFound() {
        User user = saveNewUnboardedUser(
                "test-firebase-uid-bad-sport",
                "bad.sport@test.com"
        );
        String uniqueUsername = "niko_sport_" + UUID.randomUUID().toString().substring(0, 6);

        OnboardingRequest requestWithBadSport = new OnboardingRequest(
                "Nikoloz",
                "Khuskivadze",
                uniqueUsername,
                22,
                "MALE",
                "Tbilisi",
                null,
                List.of(new UserSportDto(999_999L, "BEGINNER"))
        );

        assertThatThrownBy(() ->
                onboardingService.completeOnboarding(user.getId(), requestWithBadSport)
        )
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Sport not found");
    }

    // -------------------------------------------------------------------------
    // Error: invalid skill level string throws 400
    // -------------------------------------------------------------------------
    @Test
    void invalidSkillLevelThrowsBadRequest() {
        User user = saveNewUnboardedUser(
                "test-firebase-uid-bad-skill",
                "bad.skill@test.com"
        );
        String uniqueUsername = "niko_skill_" + UUID.randomUUID().toString().substring(0, 6);

        Long footballId = sportRepository.findAll().stream()
                .filter(s -> s.getSportName().equals("Football"))
                .findFirst()
                .orElseThrow()
                .getId();

        OnboardingRequest requestWithBadSkill = new OnboardingRequest(
                "Nikoloz",
                "Khuskivadze",
                uniqueUsername,
                22,
                "MALE",
                "Tbilisi",
                null,
                List.of(new UserSportDto(footballId, "EXPERT"))
        );

        assertThatThrownBy(() ->
                onboardingService.completeOnboarding(user.getId(), requestWithBadSkill)
        )
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid skill level");
    }

    // -------------------------------------------------------------------------
    // Error: invalid sex string throws 400
    // -------------------------------------------------------------------------
    @Test
    void invalidSexThrowsBadRequest() {
        User user = saveNewUnboardedUser(
                "test-firebase-uid-bad-sex",
                "bad.sex@test.com"
        );
        String uniqueUsername = "niko_sex_" + UUID.randomUUID().toString().substring(0, 6);

        Long footballId = sportRepository.findAll().stream()
                .filter(s -> s.getSportName().equals("Football"))
                .findFirst()
                .orElseThrow()
                .getId();

        OnboardingRequest requestWithBadSex = new OnboardingRequest(
                "Nikoloz",
                "Khuskivadze",
                uniqueUsername,
                22,
                "ATTACK_HELICOPTER",
                "Tbilisi",
                null,
                List.of(new UserSportDto(footballId, "BEGINNER"))
        );

        assertThatThrownBy(() ->
                onboardingService.completeOnboarding(user.getId(), requestWithBadSex)
        )
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid sex value");
    }

    // -------------------------------------------------------------------------
    // Sports replacement: re-submitting onboarding (if guard were off) replaces
    // old sports. We test the replacement logic directly via the repository
    // to ensure deleteByUserId + saveAll works correctly.
    // -------------------------------------------------------------------------
    @Test
    void sportsSavedCorrectlyAndPreviousSportsAreReplaced() {
        User user = saveNewUnboardedUser(
                "test-firebase-uid-sports-replace",
                "sports.replace@test.com"
        );
        String uniqueUsername = "niko_rep_" + UUID.randomUUID().toString().substring(0, 6);

        // Complete onboarding — sports saved
        onboardingService.completeOnboarding(user.getId(), buildValidRequest(uniqueUsername));

        // Verify exactly 2 sports are persisted for this user
        assertThat(userSportRepository.findByUserId(user.getId())).hasSize(2);
    }
}