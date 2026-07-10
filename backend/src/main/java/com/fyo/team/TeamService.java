package com.fyo.team;

import com.fyo.chat.ChatService;
import com.fyo.domain.*;
import com.fyo.repository.*;
import com.fyo.team.dto.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private final ChatService chatService;

    public TeamService(
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository,
            UserRepository userRepository,
            SportRepository sportRepository,
            JoinRequestRepository joinRequestRepository,
            ChatService chatService
    ) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
        this.sportRepository = sportRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.chatService = chatService;
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
    public TeamDetailsResponse createTeam(CreateTeamRequest request, Long captainUserId) {
        Sport sport = sportRepository.findById(request.sportId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sport not found"));
        User captain = userRepository.findById(captainUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Captain user not found"));

        short maxPlayers = request.maxPlayers().shortValue();
        List<User> members = resolveInitialMembers(request.memberUserIds(), captain, maxPlayers);
        short openSpots = (short) (maxPlayers - 1 - members.size());
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

        List<TeamMember> roster = new ArrayList<>();
        roster.add(teamMemberRepository.save(new TeamMember(savedTeam, captain, TeamMemberRole.CAPTAIN)));
        for (User member : members) {
            roster.add(teamMemberRepository.save(new TeamMember(savedTeam, member, TeamMemberRole.MEMBER)));
        }

        return toDetailsResponse(savedTeam, roster);
    }

    /**
     * The captain always holds a spot, so the roster can seat at most
     * `maxPlayers - 1` others. Duplicates and the captain's own id are dropped
     * rather than rejected: picking yourself is a no-op, not an error.
     */
    private List<User> resolveInitialMembers(List<Long> requestedIds, User captain, short maxPlayers) {
        if (requestedIds == null || requestedIds.isEmpty()) {
            return List.of();
        }

        List<Long> memberIds = requestedIds.stream()
                .filter(Objects::nonNull)
                .filter(id -> !id.equals(captain.getId()))
                .distinct()
                .toList();

        int otherSpots = maxPlayers - 1;
        if (memberIds.size() > otherSpots) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A roster of " + maxPlayers + " seats only " + otherSpots + " players besides the captain");
        }

        List<User> members = userRepository.findAllById(memberIds);
        if (members.size() != memberIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more selected players no longer exist");
        }
        return members;
    }

    @Transactional
    public TeamDetailsResponse joinTeam(Long id, Long userId) {
        Team team = teamRepository.findByIdAndArchivedFalseForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));
        User user = userRepository.findById(userId)
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
        chatService.addUserToTeamConversation(team.getId(), user);

        return toDetailsResponse(team, teamMemberRepository.findByTeamId(team.getId()));
    }

    @Transactional
    public JoinRequestResponse requestToJoin(Long teamId, Long userId) {
        Team team = teamRepository.findByIdAndArchivedFalse(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Where the caller already stands with this team is checked before the team's
        // own state: someone who already applied needs to hear that, not that the
        // roster has since filled up.
        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You are already on this team.");
        }
        joinRequestRepository.findByTeamIdAndUserId(teamId, userId).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, switch (existing.getStatus()) {
                case PENDING -> "You have already asked to join this team. "
                        + "The captain has not answered yet.";
                case DECLINED -> "This team declined your request to join.";
                case ACCEPTED -> "Your request to join this team was already accepted.";
            });
        });

        if (!team.isRecruiting()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This team is not recruiting right now.");
        }
        if (team.getOpenSpots() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This team has no open spots left.");
        }

        JoinRequest joinRequest = joinRequestRepository.save(new JoinRequest(team, user));
        return toJoinRequestResponse(joinRequest);
    }

    /** Teams the user plays for, captained ones included. */
    @Transactional(readOnly = true)
    public List<MyTeamResponse> getTeamsForUser(Long userId) {
        requireUser(userId);
        return teamMemberRepository.findByUserIdAndTeamArchivedFalseOrderByJoinedAtDesc(userId).stream()
                .map(member -> new MyTeamResponse(
                        toSummaryResponse(member.getTeam()),
                        member.getRole(),
                        member.getJoinedAt()
                ))
                .toList();
    }

    /**
     * The user's own join requests. Accepted ones are left out: the team they
     * unlocked already appears in {@link #getTeamsForUser}.
     */
    @Transactional(readOnly = true)
    public List<MyJoinRequestResponse> getJoinRequestsForUser(Long userId) {
        requireUser(userId);
        return joinRequestRepository
                .findByUserIdAndStatusInAndTeamArchivedFalseOrderByCreatedAtDesc(
                        userId,
                        List.of(JoinRequestStatus.PENDING, JoinRequestStatus.DECLINED)
                )
                .stream()
                .map(joinRequest -> new MyJoinRequestResponse(
                        joinRequest.getId(),
                        toSummaryResponse(joinRequest.getTeam()),
                        joinRequest.getStatus(),
                        joinRequest.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public JoinRequestResponse acceptJoinRequest(Long teamId, Long requestId, Long captainUserId) {
        JoinRequest joinRequest = joinRequestRepository.findByIdAndTeamId(requestId, teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Join request not found"));

        Team team = joinRequest.getTeam();


        if (!team.getCaptain().getId().equals(captainUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the captain can accept requests");
        }

        if (joinRequest.getStatus() != JoinRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request is not pending");
        }

        if (team.getOpenSpots() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team has no open spots");
        }

        joinRequest.accept();
        team.takeOpenSpot();
        teamMemberRepository.save(new TeamMember(team, joinRequest.getUser(), TeamMemberRole.MEMBER));
        chatService.addUserToTeamConversation(team.getId(), joinRequest.getUser());

        return toJoinRequestResponse(joinRequest);
    }

    @Transactional
    public JoinRequestResponse declineJoinRequest(Long teamId, Long requestId, Long captainUserId) {
        JoinRequest joinRequest = joinRequestRepository.findByIdAndTeamId(requestId, teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Join request not found"));

        if (!joinRequest.getTeam().getCaptain().getId().equals(captainUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the captain can decline requests");
        }

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

    private void requireCaptain(Team team, Long actingUserId) {
        if (!team.getCaptain().getId().equals(actingUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the team captain can manage join requests");
        }
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
