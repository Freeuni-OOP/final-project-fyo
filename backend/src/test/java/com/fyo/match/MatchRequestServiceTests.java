package com.fyo.match;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fyo.domain.MatchFormat;
import com.fyo.domain.MatchRequestStatus;
import com.fyo.domain.MatchStatus;
import com.fyo.domain.Sport;
import com.fyo.domain.Team;
import com.fyo.domain.User;
import com.fyo.match.dto.CreateMatchRequest;
import com.fyo.match.dto.MatchRequestResponse;
import com.fyo.match.dto.MatchResponse;
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
class MatchRequestServiceTests {

    @Autowired
    private MatchService matchService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SportRepository sportRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Test
    void acceptedUserMatchRequestCreatesMatch() {
        List<User> users = userRepository.findAll();
        User requester = users.get(0);
        User opponent = users.get(1);
        Sport sport = sportRepository.findAll().getFirst();

        MatchRequestResponse request = matchService.createMatchRequest(new CreateMatchRequest(
                sport.getId(),
                requester.getId(),
                null,
                opponent.getId(),
                null,
                "Tbilisi",
                OffsetDateTime.now().plusDays(2),
                requester.getId()
        ));

        MatchRequestResponse accepted = matchService.acceptMatchRequest(request.id(), opponent.getId());

        assertThat(accepted.status()).isEqualTo(MatchRequestStatus.ACCEPTED);
        assertThat(accepted.matchId()).isNotNull();

        MatchResponse match = matchService.getMatch(accepted.matchId());
        assertThat(match.format()).isEqualTo(MatchFormat.ONE_VS_ONE);
        assertThat(match.status()).isEqualTo(MatchStatus.UPCOMING);
        assertThat(match.home().userId()).isEqualTo(requester.getId());
        assertThat(match.away().userId()).isEqualTo(opponent.getId());
    }

    @Test
    void declinedTeamMatchRequestDoesNotCreateMatch() {
        Sport sport = sportRepository.findAll().getFirst();
        List<Team> teams = teamRepository.findAll().stream()
                .filter(team -> team.getSport().getId().equals(sport.getId()))
                .toList();
        Team requester = teams.get(0);
        Team opponent = teams.get(1);

        MatchRequestResponse request = matchService.createMatchRequest(new CreateMatchRequest(
                sport.getId(),
                null,
                requester.getId(),
                null,
                opponent.getId(),
                "Tbilisi",
                OffsetDateTime.now().plusDays(3),
                requester.getCaptain().getId()
        ));

        MatchRequestResponse declined = matchService.declineMatchRequest(request.id(), opponent.getCaptain().getId());

        assertThat(declined.status()).isEqualTo(MatchRequestStatus.DECLINED);
        assertThat(declined.matchId()).isNull();
    }

    @Test
    void acceptMatchRequestRejectsNonRecipient() {
        List<User> users = userRepository.findAll();
        User requester = users.get(0);
        User opponent = users.get(1);
        User outsider = users.get(2);
        Sport sport = sportRepository.findAll().getFirst();

        MatchRequestResponse request = matchService.createMatchRequest(new CreateMatchRequest(
                sport.getId(),
                requester.getId(),
                null,
                opponent.getId(),
                null,
                "Tbilisi",
                OffsetDateTime.now().plusDays(2),
                requester.getId()
        ));

        assertThatThrownBy(() -> matchService.acceptMatchRequest(request.id(), outsider.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only the requested user can manage this match request");
    }
}
