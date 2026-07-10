package com.fyo.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fyo.domain.User;
import com.fyo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
@Transactional
Class AdminServiceTests() {
    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    private User getAdminUser() {
        return userRepository.findAll().stream()
                .filter(User::isAdmin)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No admin user in test data"));
    }

    private User getNonAdminUser() {
        return userRepository.findAll().stream()
                .filter(u -> !u.isAdmin())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No non-admin user in test data"));
    }
}