package com.fyo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.fyo.domain.Match;
import com.fyo.domain.MatchResult;
import com.fyo.domain.Sport;
import com.fyo.domain.User;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MatchCascadeTests {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MatchResultRepository matchResultRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SportRepository sportRepository;

    @Test
    void deletingMatchRemovesItsMatchResult() {
        List<User> users = userRepository.findAll();
        User home = users.get(0);
        User away = users.get(1);
        Sport sport = sportRepository.findAll().getFirst();

        Match match = matchRepository.save(Match.oneVsOne(
                sport,
                home,
                away,
                "Tbilisi",
                OffsetDateTime.now().plusDays(1)
        ));
        MatchResult result = matchResultRepository.save(new MatchResult(match, (short) 2, (short) 1, home));
        Long matchId = match.getId();
        Long resultId = result.getId();

        Match reloaded = matchRepository.findById(matchId).orElseThrow();
        assertThat(reloaded.getResult()).isNotNull();
        assertThat(reloaded.getResult().getId()).isEqualTo(resultId);

        matchRepository.delete(reloaded);
        matchRepository.flush();

        assertThat(matchRepository.findById(matchId)).isEmpty();
        assertThat(matchResultRepository.findById(resultId)).isEmpty();
        assertThat(matchResultRepository.findByMatchId(matchId)).isEmpty();
    }
}
