package com.fyo.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fyo.admin.dto.CreateSportRequest;
import com.fyo.admin.dto.SportAdminResponse;
import com.fyo.auth.CurrentUserService;
import com.fyo.domain.User;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdminControllerTests {

    private final AdminService adminService = mock(AdminService.class);
    private final CurrentUserService currentUserService = mock(CurrentUserService.class);
    private final AdminController controller = new AdminController(adminService, currentUserService);

    private static final User ADMIN = new User("uid-admin", "Admin", "User", "admin", "admin@example.com");

    @Test
    void getUsersUsesAuthenticatedAdmin() {
        when(currentUserService.requireCurrentUser("Bearer token")).thenReturn(ADMIN);
        when(adminService.getUsers(ADMIN.getId())).thenReturn(List.of());

        assertThat(controller.getUsers("Bearer token")).isEmpty();
        verify(adminService).getUsers(ADMIN.getId());
    }

    @Test
    void createSportUsesAuthenticatedAdmin() {
        CreateSportRequest body = new CreateSportRequest("Padel");
        SportAdminResponse created = new SportAdminResponse(1L, "Padel");
        when(currentUserService.requireCurrentUser("Bearer token")).thenReturn(ADMIN);
        when(adminService.createSport(ADMIN.getId(), body)).thenReturn(created);

        assertThat(controller.createSport("Bearer token", body)).isEqualTo(created);
    }
}
