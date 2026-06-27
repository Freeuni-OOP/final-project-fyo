package com.fyo.team;

import com.fyo.team.dto.CreateTeamRequest;
import com.fyo.team.dto.JoinTeamRequest;
import com.fyo.team.dto.TeamDetailsResponse;
import com.fyo.team.dto.TeamSummaryResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
}
