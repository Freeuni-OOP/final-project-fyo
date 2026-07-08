package com.fyo.match;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fyo.domain.Match;
import com.fyo.domain.MatchFormat;
import com.fyo.domain.MatchStatus;
import com.fyo.domain.Sport;
import com.fyo.domain.Team;
import com.fyo.domain.User;
import com.fyo.match.dto.MatchResponse;
import com.fyo.repository.MatchRepository;
import com.fyo.repository.SportRepository;
import com.fyo.repository.TeamRepository;
import com.fyo.repository.UserRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@Transactional
class MatchServiceTests {

    @Autowired
    private MatchService matchService;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private SportRepository sportRepository;

    @Test
    void getMatchesReturnsSeededMatchesWhenUnfiltered() {
        List<MatchResponse> matches = matchService.getMatches(null, null);

        assertThat(matches).isNotEmpty();
        assertThat(matches)
                .allSatisfy(m -> {
                    assertThat(m.id()).isNotNull();
                    assertThat(m.sport()).isNotNull();
                    assertThat(m.home()).isNotNull();
                    assertThat(m.away()).isNotNull();
                    assertThat(m.format()).isNotNull();
                    assertThat(m.status()).isNotNull();
                });
    }

    @Test
    void getMatchesFiltersByTeamId() {
        Team team = teamRepository.findAll().stream()
                .filter(t -> !t.isArchived())
                .findFirst()
                .orElseThrow();

        List<MatchResponse> matches = matchService.getMatches(null, team.getId());

        assertThat(matches).isNotEmpty();
        assertThat(matches).allMatch(m ->
                m.format() == MatchFormat.TEAM_VS_TEAM
                        && (team.getId().equals(m.home().teamId()) || team.getId().equals(m.away().teamId()))
        );
    }

    @Test
    void getMatchesFiltersByUserIdForOneVsOne() {
        List<User> users = userRepository.findAll();
        User home = users.get(0);
        User away = users.get(1);
        Sport sport = sportRepository.findAll().getFirst();

        Match saved = matchRepository.save(
                Match.oneVsOne(sport, home, away, "Test court", OffsetDateTime.now().plusDays(1))
        );

        List<MatchResponse> forHome = matchService.getMatches(home.getId(), null);

        assertThat(forHome).anyMatch(m -> m.id().equals(saved.getId()));
        assertThat(forHome.stream().filter(m -> m.id().equals(saved.getId())).findFirst())
                .get()
                .satisfies(m -> {
                    assertThat(m.format()).isEqualTo(MatchFormat.ONE_VS_ONE);
                    assertThat(m.home().userId()).isEqualTo(home.getId());
                    assertThat(m.away().userId()).isEqualTo(away.getId());
                    assertThat(m.home().teamId()).isNull();
                    assertThat(m.away().teamId()).isNull();
                });
    }

    @Test
    void getMatchReturnsDetails() {
        Match existing = matchRepository.findAll().getFirst();

        MatchResponse response = matchService.getMatch(existing.getId());

        assertThat(response.id()).isEqualTo(existing.getId());
        assertThat(response.location()).isEqualTo(existing.getLocation());
        assertThat(response.status()).isEqualTo(existing.getStatus());
    }

    @Test
    void getMatchRejectsUnknownId() {
        assertThatThrownBy(() -> matchService.getMatch(-1L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(rse.getReason()).contains("Match not found");
                });
    }

    @Test
    void cancelMatchAsCaptainSucceedsForUpcomingTeamMatch() {
        Match upcoming = matchRepository.findAll().stream()
                .filter(m -> m.getStatus() == MatchStatus.UPCOMING)
                .filter(m -> m.getFormat() == MatchFormat.TEAM_VS_TEAM)
                .findFirst()
                .orElseThrow();

        Long captainId = upcoming.getHomeTeam().getCaptain().getId();

        MatchResponse cancelled = matchService.cancelMatch(upcoming.getId(), captainId);

        assertThat(cancelled.status()).isEqualTo(MatchStatus.CANCELLED);
        assertThat(matchRepository.findById(upcoming.getId()))
                .get()
                .extracting(Match::getStatus)
                .isEqualTo(MatchStatus.CANCELLED);
    }

    @Test
    void cancelMatchAsOneVsOneParticipantSucceeds() {
        List<User> users = userRepository.findAll();
        User home = users.get(0);
        User away = users.get(1);
        Sport sport = sportRepository.findAll().getFirst();

        Match saved = matchRepository.save(
                Match.oneVsOne(sport, home, away, "Cancel court", OffsetDateTime.now().plusDays(2))
        );

        MatchResponse cancelled = matchService.cancelMatch(saved.getId(), away.getId());

        assertThat(cancelled.status()).isEqualTo(MatchStatus.CANCELLED);
    }

    @Test
    void cancelMatchRejectsNonParticipant() {
        Match upcoming = matchRepository.findAll().stream()
                .filter(m -> m.getStatus() == MatchStatus.UPCOMING)
                .filter(m -> m.getFormat() == MatchFormat.TEAM_VS_TEAM)
                .findFirst()
                .orElseThrow();

        Long homeCaptainId = upcoming.getHomeTeam().getCaptain().getId();
        Long awayCaptainId = upcoming.getAwayTeam().getCaptain().getId();
        User outsider = userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(homeCaptainId) && !u.getId().equals(awayCaptainId))
                .findFirst()
                .orElseThrow();

        assertThatThrownBy(() -> matchService.cancelMatch(upcoming.getId(), outsider.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(rse.getReason()).contains("not a participant");
                });
    }

    @Test
    void cancelMatchRejectsNonUpcomingMatch() {
        Match upcoming = matchRepository.findAll().stream()
                .filter(m -> m.getStatus() == MatchStatus.UPCOMING)
                .filter(m -> m.getFormat() == MatchFormat.TEAM_VS_TEAM)
                .findFirst()
                .orElseThrow();
        Long captainId = upcoming.getHomeTeam().getCaptain().getId();

        matchService.cancelMatch(upcoming.getId(), captainId);

        assertThatThrownBy(() -> matchService.cancelMatch(upcoming.getId(), captainId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(rse.getReason()).contains("Only an upcoming match can be cancelled");
                });
    }
}
