package com.fyo.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fyo.admin.dto.CreateSportRequest;
import com.fyo.admin.dto.SportAdminResponse;
import com.fyo.admin.dto.TeamAdminResponse;
import com.fyo.admin.dto.UserAdminResponse;
import com.fyo.domain.User;
import com.fyo.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    @Test
    void adminCanArchiveUser() {
        User admin = getAdminUser();
        User target = getNonAdminUser();

        UserAdminResponse archived = adminService.archiveUser(admin.getId(), target.getId());

        assertThat(archived.archived()).isTrue();
        assertThat(archived.archivedAt()).isNotNull();
    }

    @Test
    void archivingAlreadyArchivedUserThrows() {
        User admin = getAdminUser();
        User target = getNonAdminUser();

        adminService.archiveUser(admin.getId(), target.getId());

        assertThatThrownBy(() -> adminService.archiveUser(admin.getId(), target.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User is already archived");
    }

    @Test
    void adminCanArchiveTeam() {
        User admin = getAdminUser();

        List<TeamAdminResponse> teams = adminService.getTeams(admin.getId());
        if (teams.isEmpty()) return;

        TeamAdminResponse target = teams.stream()
                .filter(t -> !t.archived())
                .findFirst()
                .orElse(null);
        if (target == null) return;

        TeamAdminResponse archived = adminService.archiveTeam(admin.getId(), target.id());

        assertThat(archived.archived()).isTrue();
    }
}