package com.fyo.team;

import com.fyo.team.dto.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
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
    public JoinRequestResponse requestToJoin(@PathVariable Long id, @RequestParam Long userId) {
        return teamService.requestToJoin(id, userId);
    }

    @GetMapping("/{id}/join-requests")
    public List<JoinRequestResponse> getPendingJoinRequests(@PathVariable Long id) {
        return teamService.getPendingJoinRequests(id);
    }

    @PostMapping("/{id}/join-requests/{requestId}/accept")
    public JoinRequestResponse acceptJoinRequest(@PathVariable Long id, @PathVariable Long requestId) {
        return teamService.acceptJoinRequest(id, requestId);
    }

    @PostMapping("/{id}/join-requests/{requestId}/decline")
    public JoinRequestResponse declineJoinRequest(@PathVariable Long id, @PathVariable Long requestId) {
        return teamService.declineJoinRequest(id, requestId);
    }
}
