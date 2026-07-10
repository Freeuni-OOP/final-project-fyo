package com.fyo.admin;

import com.fyo.admin.dto.CreateSportRequest;
import com.fyo.admin.dto.SportAdminResponse;
import com.fyo.admin.dto.TeamAdminResponse;
import com.fyo.admin.dto.UserAdminResponse;
import com.fyo.auth.CurrentUserService;
import com.fyo.domain.User;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final CurrentUserService currentUserService;

    public AdminController(AdminService adminService, CurrentUserService currentUserService) {
        this.adminService = adminService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/users")
    public List<UserAdminResponse> getUsers(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        User admin = currentUserService.requireCurrentUser(authorization);
        return adminService.getUsers(admin.getId());
    }

    @DeleteMapping("/users/{id}")
    public UserAdminResponse archiveUser(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id
    ) {
        User admin = currentUserService.requireCurrentUser(authorization);
        return adminService.archiveUser(admin.getId(), id);
    }

    @GetMapping("/teams")
    public List<TeamAdminResponse> getTeams(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        User admin = currentUserService.requireCurrentUser(authorization);
        return adminService.getTeams(admin.getId());
    }

    @DeleteMapping("/teams/{id}")
    public TeamAdminResponse archiveTeam(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id
    ) {
        User admin = currentUserService.requireCurrentUser(authorization);
        return adminService.archiveTeam(admin.getId(), id);
    }

    @GetMapping("/sports")
    public List<SportAdminResponse> getSports(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        User admin = currentUserService.requireCurrentUser(authorization);
        return adminService.getSports(admin.getId());
    }

    @PostMapping("/sports")
    @ResponseStatus(HttpStatus.CREATED)
    public SportAdminResponse createSport(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody CreateSportRequest request
    ) {
        User admin = currentUserService.requireCurrentUser(authorization);
        return adminService.createSport(admin.getId(), request);
    }

    @DeleteMapping("/sports/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSport(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id
    ) {
        User admin = currentUserService.requireCurrentUser(authorization);
        adminService.deleteSport(admin.getId(), id);
    }
}
