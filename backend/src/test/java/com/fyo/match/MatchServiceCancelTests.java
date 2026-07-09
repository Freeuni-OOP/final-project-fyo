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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@Transactional
class MatchServiceCancelTests {

    @Autowired
    private MatchService matchService;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SportRepository sportRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Test
    void cancelTeamMatchAllowsCaptain() {
        List<User> users = userRepository.findAll();
        User homeCaptain = users.get(0);
        Sport sport = sportRepository.findAll().getFirst();
        List<Team> teams = teamRepository.findAll().stream()
                .filter(team -> team.getSport().getId().equals(sport.getId()))
                .toList();
        Team home = teams.stream().filter(team -> team.getCaptain().getId().equals(homeCaptain.getId())).findFirst()
                .orElse(teams.get(0));
        Team away = teams.stream().filter(team -> !team.getId().equals(home.getId())).findFirst().orElseThrow();

        Match match = matchRepository.save(Match.teamVsTeam(
                sport,
                home,
                away,
                "Tbilisi",
                OffsetDateTime.now().plusDays(1)
        ));

        MatchResponse cancelled = matchService.cancelMatch(match.getId(), home.getCaptain().getId());

        assertThat(cancelled.status()).isEqualTo(MatchStatus.CANCELLED);
        assertThat(cancelled.format()).isEqualTo(MatchFormat.TEAM_VS_TEAM);
    }

    @Test
    void cancelTeamMatchRejectsNonCaptainMemberExplicitly() {
        List<User> users = userRepository.findAll();
        Sport sport = sportRepository.findAll().getFirst();
        List<Team> teams = teamRepository.findAll().stream()
                .filter(team -> team.getSport().getId().equals(sport.getId()))
                .toList();
        Team home = teams.get(0);
        Team away = teams.get(1);
        Long homeCaptainId = home.getCaptain().getId();
        User nonCaptain = users.stream()
                .filter(user -> !user.getId().equals(homeCaptainId) && !user.getId().equals(away.getCaptain().getId()))
                .findFirst()
                .orElseThrow();

        Match match = matchRepository.save(Match.teamVsTeam(
                sport,
                home,
                away,
                "Tbilisi",
                OffsetDateTime.now().plusDays(1)
        ));

        assertThatThrownBy(() -> matchService.cancelMatch(match.getId(), nonCaptain.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only a team captain can cancel a team match");
    }
}
