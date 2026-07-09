package com.fyo.repository;

import com.fyo.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByFirebaseUid(String firebaseUid);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    List<User> findAllByOrderByCreatedAtDesc();

    /**
     * Matches a lowercased `%term%` pattern against the username and the full
     * name. `!` is the escape character so a term containing `%` or `_` stays a
     * literal instead of widening the search.
     */
    @Query("""
            SELECT u FROM User u
            WHERE u.archived = false
              AND (LOWER(u.username) LIKE :pattern ESCAPE '!'
                   OR LOWER(CONCAT(u.name, ' ', u.surname)) LIKE :pattern ESCAPE '!')
            ORDER BY u.username
            """)
    List<User> searchActive(@Param("pattern") String pattern, Pageable pageable);
}
