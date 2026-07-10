package com.fyo.team;

import com.fyo.auth.CurrentUserService;
import com.fyo.domain.User;
import com.fyo.team.dto.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;
    private final CurrentUserService currentUserService;

    public TeamController(TeamService teamService, CurrentUserService currentUserService) {
        this.teamService = teamService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<TeamSummaryResponse> getTeams() {
        return teamService.getTeams();
    }

    @GetMapping("/{id}")
    public TeamDetailsResponse getTeam(@PathVariable Long id) {
        return teamService.getTeam(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamDetailsResponse createTeam(@Valid @RequestBody CreateTeamRequest request) {
        return teamService.createTeam(request);
    }

    @PostMapping("/{id}/join")
    public TeamDetailsResponse joinTeam(@PathVariable Long id, @Valid @RequestBody JoinTeamRequest request) {
        return teamService.joinTeam(id, request);
    }

    @PostMapping("/{id}/join-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public JoinRequestResponse requestToJoin(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        return teamService.requestToJoin(id, currentUser.getId());
    }

    @GetMapping("/{id}/join-requests")
    public List<JoinRequestResponse> getPendingJoinRequests(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id
    ) {
        currentUserService.requireCurrentUser(authorization);
        return teamService.getPendingJoinRequests(id);
    }

    @PostMapping("/{id}/join-requests/{requestId}/accept")
    public JoinRequestResponse acceptJoinRequest(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @PathVariable Long requestId
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        return teamService.acceptJoinRequest(id, requestId, currentUser.getId());
    }

    @PostMapping("/{id}/join-requests/{requestId}/decline")
    public JoinRequestResponse declineJoinRequest(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @PathVariable Long requestId
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        return teamService.declineJoinRequest(id, requestId, currentUser.getId());
    }
}