package com.fyo.match;

import com.fyo.domain.Match;
import com.fyo.domain.Sport;
import com.fyo.domain.Team;
import com.fyo.domain.User;
import com.fyo.match.dto.MatchParticipantResponse;
import com.fyo.match.dto.MatchResponse;
import com.fyo.match.dto.SportResponse;
import com.fyo.repository.MatchRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MatchService {

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
            return new MatchParticipantResponse(user.getId(), null, user.getName() + " " + user.getSurname(), user.getImageUrl());
        }
        return new MatchParticipantResponse(null, team.getId(), team.getName(), team.getLogoUrl());
    }
}