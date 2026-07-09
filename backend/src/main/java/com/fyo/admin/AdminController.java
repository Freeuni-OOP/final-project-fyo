package com.fyo.admin;

import com.fyo.admin.dto.CreateSportRequest;
import com.fyo.admin.dto.SportAdminResponse;
import com.fyo.admin.dto.TeamAdminResponse;
import com.fyo.admin.dto.UserAdminResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public List<UserAdminResponse> getUsers(@RequestParam Long adminUserId) {
        return adminService.getUsers(adminUserId);
    }

    @DeleteMapping("/users/{id}")
    public UserAdminResponse archiveUser(@RequestParam Long adminUserId, @PathVariable Long id) {
        return adminService.archiveUser(adminUserId, id);
    }

    @GetMapping("/teams")
    public List<TeamAdminResponse> getTeams(@RequestParam Long adminUserId) {
        return adminService.getTeams(adminUserId);
    }

    @DeleteMapping("/teams/{id}")
    public TeamAdminResponse archiveTeam(@RequestParam Long adminUserId, @PathVariable Long id) {
        return adminService.archiveTeam(adminUserId, id);
    }

    @GetMapping("/sports")
    public List<SportAdminResponse> getSports(@RequestParam Long adminUserId) {
        return adminService.getSports(adminUserId);
    }

    @PostMapping("/sports")
    @ResponseStatus(HttpStatus.CREATED)
    public SportAdminResponse createSport(@RequestParam Long adminUserId, @Valid @RequestBody CreateSportRequest request) {
        return adminService.createSport(adminUserId, request);
    }

    @DeleteMapping("/sports/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSport(@RequestParam Long adminUserId, @PathVariable Long id) {
        adminService.deleteSport(adminUserId, id);
    }
}