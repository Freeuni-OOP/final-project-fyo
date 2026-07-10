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

    @Test
    void nonAdminCannotGetUsers() {
        User nonAdmin = getNonAdminUser();

        assertThatThrownBy(() -> adminService.getUsers(nonAdmin.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User is not an admin");
    }

    @Test
    void nonAdminCannotGetTeams() {
        User nonAdmin = getNonAdminUser();

        assertThatThrownBy(() -> adminService.getTeams(nonAdmin.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User is not an admin");
    }

    @Test
    void nonAdminCannotGetSports() {
        User nonAdmin = getNonAdminUser();

        assertThatThrownBy(() -> adminService.getSports(nonAdmin.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User is not an admin");
    }

    @Test
    void adminCanGetUsers() {
        User admin = getAdminUser();

        List<UserAdminResponse> users = adminService.getUsers(admin.getId());

        assertThat(users).isNotEmpty();
    }

    @Test
    void adminCanGetTeams() {
        User admin = getAdminUser();

        List<TeamAdminResponse> teams = adminService.getTeams(admin.getId());

        assertThat(teams).isNotNull();
    }

    @Test
    void adminCanGetSports() {
        User admin = getAdminUser();

        List<SportAdminResponse> sports = adminService.getSports(admin.getId());

        assertThat(sports).isNotEmpty();
    }
}