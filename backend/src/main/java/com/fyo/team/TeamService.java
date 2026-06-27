package com.fyo.team;

import com.fyo.domain.*;
import com.fyo.repository.*;
import com.fyo.team.dto.*;

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
    private final JoinRequestRepository joinRequestRepository;

    public TeamService(
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository,
            UserRepository userRepository,
            SportRepository sportRepository,
            JoinRequestRepository joinRequestRepository
    ) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
        this.sportRepository = sportRepository;
        this.joinRequestRepository = joinRequestRepository;
    }

    @Transactional(readOnly = true)
    public List<TeamSummaryResponse> getTeams() {
        return teamRepository.findByArchivedFalse().stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TeamDetailsResponse getTeam(Long id) {
        Team team = teamRepository.findByIdAndArchivedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));
        return toDetailsResponse(team, teamMemberRepository.findByTeamId(team.getId()));
    }

    @Transactional
    public TeamDetailsResponse createTeam(CreateTeamRequest request) {
        Sport sport = sportRepository.findById(request.sportId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sport not found"));
        User captain = userRepository.findById(request.captainUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Captain user not found"));

        short maxPlayers = request.maxPlayers().shortValue();
        short openSpots = (short) (maxPlayers - 1);
        boolean recruiting = request.isRecruiting() == null || request.isRecruiting();

        Team team = new Team(
                request.name().trim(),
                sport,
                request.region(),
                request.description(),
                request.logoUrl(),
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
        Team team = teamRepository.findByIdAndArchivedFalseForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!team.isRecruiting()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team is not recruiting");
        }
        if (team.getOpenSpots() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team has no open spots");
        }
        if (teamMemberRepository.existsByTeamIdAndUserId(team.getId(), user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a team member");
        }

        team.takeOpenSpot();
        teamMemberRepository.save(new TeamMember(team, user, TeamMemberRole.MEMBER));

        return toDetailsResponse(team, teamMemberRepository.findByTeamId(team.getId()));
    }

    @Transactional
    public JoinRequestResponse requestToJoin(Long teamId, Long userId) {
        Team team = teamRepository.findByIdAndArchivedFalse(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!team.isRecruiting()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team is not recruiting");
        }
        if (team.getOpenSpots() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team has no open spots");
        }
        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a team member");
        }
        if (joinRequestRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Join request already exists");
        }

        JoinRequest joinRequest = joinRequestRepository.save(new JoinRequest(team, user));
        return toJoinRequestResponse(joinRequest);
    }

    @Transactional
    public JoinRequestResponse acceptJoinRequest(Long teamId, Long requestId) {
        JoinRequest joinRequest = joinRequestRepository.findByIdAndTeamId(requestId, teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Join request not found"));

        if (joinRequest.getStatus() != JoinRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request is not pending");
        }

        Team team = joinRequest.getTeam();
        if (team.getOpenSpots() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team has no open spots");
        }

        joinRequest.accept();
        team.takeOpenSpot();
        teamMemberRepository.save(new TeamMember(team, joinRequest.getUser(), TeamMemberRole.MEMBER));

        return toJoinRequestResponse(joinRequest);
    }

    @Transactional
    public JoinRequestResponse declineJoinRequest(Long teamId, Long requestId) {
        JoinRequest joinRequest = joinRequestRepository.findByIdAndTeamId(requestId, teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Join request not found"));

        if (joinRequest.getStatus() != JoinRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request is not pending");
        }

        joinRequest.decline();
        return toJoinRequestResponse(joinRequest);
    }

    @Transactional(readOnly = true)
    public List<JoinRequestResponse> getPendingJoinRequests(Long teamId) {
        teamRepository.findByIdAndArchivedFalse(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));
        return joinRequestRepository.findByTeamIdAndStatus(teamId, JoinRequestStatus.PENDING)
                .stream()
                .map(this::toJoinRequestResponse)
                .toList();
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

    private JoinRequestResponse toJoinRequestResponse(JoinRequest joinRequest) {
        User user = joinRequest.getUser();
        return new JoinRequestResponse(
                joinRequest.getId(),
                joinRequest.getTeam().getId(),
                user.getId(),
                user.getUsername(),
                user.getName() + " " + user.getSurname(),
                user.getImageUrl(),
                joinRequest.getStatus(),
                joinRequest.getCreatedAt()
        );
    }

}
