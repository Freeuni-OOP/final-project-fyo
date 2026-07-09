package com.fyo.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fyo.domain.User;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Runs against the local dev database, like TeamServiceTests; every test rolls
 * back, so nothing it inserts survives.
 */
@SpringBootTest
@Transactional
class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByFirebaseUidReturnsSavedUser() {
        User saved = userRepository.save(newUser());

        assertThat(userRepository.findByFirebaseUid(saved.getFirebaseUid()))
                .hasValueSatisfying(found -> {
                    assertThat(found.getId()).isEqualTo(saved.getId());
                    assertThat(found.getEmail()).isEqualTo(saved.getEmail());
                });
    }

    @Test
    void findByFirebaseUidIsEmptyForUnknownUid() {
        assertThat(userRepository.findByFirebaseUid("uid-that-does-not-exist")).isEmpty();
    }

    @Test
    void existsByEmailMatchesExactEmailOnly() {
        User saved = userRepository.save(newUser());

        assertThat(userRepository.existsByEmail(saved.getEmail())).isTrue();
        assertThat(userRepository.existsByEmail("other-" + saved.getEmail())).isFalse();
    }

    @Test
    void existsByUsernameMatchesExactUsernameOnly() {
        User saved = userRepository.save(newUser());

        assertThat(userRepository.existsByUsername(saved.getUsername())).isTrue();
        assertThat(userRepository.existsByUsername(saved.getUsername() + "2")).isFalse();
    }

    @Test
    void emailColumnRejectsDuplicates() {
        User first = userRepository.saveAndFlush(newUser());

        User duplicate = newUser();
        duplicate.setEmail(first.getEmail());

        assertThatThrownBy(() -> userRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void firebaseUidColumnRejectsDuplicates() {
        User first = userRepository.saveAndFlush(newUser());

        User duplicate = newUser();
        duplicate.setFirebaseUid(first.getFirebaseUid());

        assertThatThrownBy(() -> userRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private User newUser() {
        String unique = "test-" + UUID.randomUUID();
        return new User(unique, "Test", "User", "user-" + unique, unique + "@example.com");
    }
}
