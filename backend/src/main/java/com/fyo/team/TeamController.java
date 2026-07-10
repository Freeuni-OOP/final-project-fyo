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

    /** Declared before `/{id}`, which would otherwise try to parse "mine" as an id. */
    @GetMapping("/mine")
    public List<MyTeamResponse> getMyTeams(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        return teamService.getTeamsForUser(currentUser.getId());
    }

    @GetMapping("/my-requests")
    public List<MyJoinRequestResponse> getMyJoinRequests(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        return teamService.getJoinRequestsForUser(currentUser.getId());
    }

    @GetMapping("/{id}")
    public TeamDetailsResponse getTeam(@PathVariable Long id) {
        return teamService.getTeam(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamDetailsResponse createTeam(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody CreateTeamRequest request
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        return teamService.createTeam(request, currentUser.getId());
    }

    @PostMapping("/{id}/join")
    public TeamDetailsResponse joinTeam(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        return teamService.joinTeam(id, currentUser.getId());
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
    public List<JoinRequestResponse> getPendingJoinRequests(@PathVariable Long id) {
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
