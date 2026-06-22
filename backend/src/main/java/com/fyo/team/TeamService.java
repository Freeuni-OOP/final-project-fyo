package com.fyo.team;

import com.fyo.domain.Sport;
import com.fyo.domain.Team;
import com.fyo.domain.TeamMember;
import com.fyo.domain.TeamMemberRole;
import com.fyo.domain.User;
import com.fyo.repository.SportRepository;
import com.fyo.repository.TeamMemberRepository;
import com.fyo.repository.TeamRepository;
import com.fyo.repository.UserRepository;
import com.fyo.team.dto.CreateTeamRequest;
import com.fyo.team.dto.SportResponse;
import com.fyo.team.dto.JoinTeamRequest;
import com.fyo.team.dto.TeamDetailsResponse;
import com.fyo.team.dto.TeamMemberResponse;
import com.fyo.team.dto.TeamSummaryResponse;
import com.fyo.team.dto.UserSummaryResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final SportRepository sportRepository;

    public TeamService(
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository,
            UserRepository userRepository,
            SportRepository sportRepository
    ) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
        this.sportRepository = sportRepository;
    }

    @Transactional(readOnly = true)
    public List<TeamSummaryResponse> getTeams() {
        return teamRepository.findByArchivedFalseOrderByCreatedAtDesc().stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TeamDetailsResponse getTeam(Long id) {
        Team team = getActiveTeam(id);
        return toDetailsResponse(team, teamMemberRepository.findByTeamIdOrderByRoleAscJoinedAtAscIdAsc(team.getId()));
    }

    @Transactional
    public TeamDetailsResponse createTeam(CreateTeamRequest request) {
        Sport sport = sportRepository.findById(request.sportId())
                .orElseThrow(() -> notFound("Sport not found"));
        User captain = userRepository.findById(request.captainUserId())
                .orElseThrow(() -> notFound("Captain user not found"));

        if (captain.isArchived()) {
            throw badRequest("Captain user is archived");
        }

        short maxPlayers = request.maxPlayers().shortValue();
        short openSpots = (short) (maxPlayers - 1);
        boolean recruiting = request.isRecruiting() == null || request.isRecruiting();

        Team team = new Team(
                request.name().trim(),
                sport,
                normalizeBlank(request.region()),
                normalizeBlank(request.description()),
                normalizeBlank(request.logoUrl()),
                captain,
                maxPlayers,
                openSpots,
                recruiting
        );
        Team savedTeam = teamRepository.save(team);
        TeamMember captainMember = teamMemberRepository.save(
                new TeamMember(savedTeam, captain, TeamMemberRole.CAPTAIN)
        );

        return toDetailsResponse(savedTeam, List.of(captainMember));
    }

    @Transactional
    public TeamDetailsResponse joinTeam(Long id, JoinTeamRequest request) {
        Team team = getActiveTeam(id);
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> notFound("User not found"));

        if (user.isArchived()) {
            throw badRequest("User is archived");
        }
        if (!team.isRecruiting()) {
            throw badRequest("Team is not recruiting");
        }
        if (team.getOpenSpots() <= 0) {
            throw badRequest("Team has no open spots");
        }
        if (teamMemberRepository.existsByTeamIdAndUserId(team.getId(), user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a team member");
        }

        team.takeOpenSpot();
        teamMemberRepository.save(new TeamMember(team, user, TeamMemberRole.MEMBER));

        return toDetailsResponse(team, teamMemberRepository.findByTeamIdOrderByRoleAscJoinedAtAscIdAsc(team.getId()));
    }

    private Team getActiveTeam(Long id) {
        return teamRepository.findByIdAndArchivedFalse(id)
                .orElseThrow(() -> notFound("Team not found"));
    }

    private TeamSummaryResponse toSummaryResponse(Team team) {
        return new TeamSummaryResponse(
                team.getId(),
                team.getName(),
                toSportResponse(team.getSport()),
                team.getRegion(),
                team.getDescription(),
                team.getLogoUrl(),
                toUserSummaryResponse(team.getCaptain()),
                team.getMaxPlayers(),
                team.getOpenSpots(),
                team.isRecruiting()
        );
    }

    private TeamDetailsResponse toDetailsResponse(Team team, List<TeamMember> members) {
        return new TeamDetailsResponse(
                team.getId(),
                team.getName(),
                toSportResponse(team.getSport()),
                team.getRegion(),
                team.getDescription(),
                team.getLogoUrl(),
                toUserSummaryResponse(team.getCaptain()),
                team.getMaxPlayers(),
                team.getOpenSpots(),
                team.isRecruiting(),
                team.getCreatedAt(),
                members.stream().map(this::toMemberResponse).toList()
        );
    }

    private SportResponse toSportResponse(Sport sport) {
        return new SportResponse(sport.getId(), sport.getSportName());
    }

    private UserSummaryResponse toUserSummaryResponse(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getSurname(),
                user.getRegion(),
                user.getImageUrl()
        );
    }

    private TeamMemberResponse toMemberResponse(TeamMember member) {
        User user = member.getUser();
        return new TeamMemberResponse(
                member.getId(),
                user.getId(),
                user.getUsername(),
                user.getName() + " " + user.getSurname(),
                user.getImageUrl(),
                member.getRole(),
                member.getJoinedAt()
        );
    }

    private String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private ResponseStatusException notFound(String reason) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, reason);
    }

    private ResponseStatusException badRequest(String reason) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
    }
}
