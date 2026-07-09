package com.fyo.repository;

import com.fyo.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByFirebaseUid(String firebaseUid);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
    
    List<User> findAllByOrderByCreatedAtDesc();
}
