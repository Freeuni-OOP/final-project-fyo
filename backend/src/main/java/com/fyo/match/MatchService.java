package com.fyo.match;

import com.fyo.domain.Match;
import com.fyo.domain.Sport;
import com.fyo.domain.Team;
import com.fyo.domain.User;
import com.fyo.domain.MatchFormat;
import com.fyo.domain.MatchStatus;
import com.fyo.match.dto.MatchParticipantResponse;
import com.fyo.match.dto.MatchResponse;
import com.fyo.repository.MatchRepository;
import com.fyo.team.dto.SportResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MatchService {

    private static final Logger log = LoggerFactory.getLogger(MatchService.class);

    private final MatchRepository matchRepository;

    public MatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> getMatches(Long userId, Long teamId) {
        List<Match> matches;
        if (userId != null) {
            matches = matchRepository.findByHomeUserIdOrAwayUserId(userId, userId);
        } else if (teamId != null) {
            matches = matchRepository.findByHomeTeamIdOrAwayTeamId(teamId, teamId);
        } else {
            matches = matchRepository.findAll();
        }
        return matches.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MatchResponse getMatch(Long id) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found"));
        return toResponse(match);
    }

    @Transactional
    public MatchResponse cancelMatch(Long id, Long actingUserId) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found"));

        assertCanCancel(match, actingUserId);
        if (match.getStatus() != MatchStatus.UPCOMING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only an upcoming match can be cancelled");
        }

        match.cancel();
        return toResponse(match);
    }

    /**
     * ONE_VS_ONE: either participant may cancel.
     * TEAM_VS_TEAM: only a captain of the home or away team may cancel —
     * ordinary team members are rejected explicitly (not treated as participants).
     */
    private void assertCanCancel(Match match, Long actingUserId) {
        if (match.getFormat() == MatchFormat.ONE_VS_ONE) {
            if (actingUserId.equals(idOf(match.getHomeUser())) || actingUserId.equals(idOf(match.getAwayUser()))) {
                return;
            }
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a participant in this match");
        }

        if (actingUserId.equals(captainIdOf(match.getHomeTeam())) || actingUserId.equals(captainIdOf(match.getAwayTeam()))) {
            return;
        }
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Only a team captain can cancel a team match"
        );
    }

    private Long idOf(User user) {
        return user == null ? null : user.getId();
    }

    private Long captainIdOf(Team team) {
        return team == null || team.getCaptain() == null ? null : team.getCaptain().getId();
    }

    private MatchResponse toResponse(Match match) {
        return new MatchResponse(
                match.getId(),
                toSportResponse(match.getSport()),
                match.getFormat(),
                toParticipantResponse(match.getHomeUser(), match.getHomeTeam()),
                toParticipantResponse(match.getAwayUser(), match.getAwayTeam()),
                match.getLocation(),
                match.getProposedDatetime(),
                match.getStatus(),
                match.getCreatedAt()
        );
    }

    private SportResponse toSportResponse(Sport sport) {
        return new SportResponse(sport.getId(), sport.getSportName());
    }

    private MatchParticipantResponse toParticipantResponse(User user, Team team) {
        if (user != null) {
            return new MatchParticipantResponse(
                    user.getId(),
                    null,
                    user.getName() + " " + user.getSurname(),
                    user.getImageUrl()
            );
        }
        if (team == null) {
            log.error("Corrupted match state: side is missing both user and team");
            throw new IllegalStateException("Match side is missing both user and team");
        }
        return new MatchParticipantResponse(null, team.getId(), team.getName(), team.getLogoUrl());
    }
}
