package com.fyo.user;

import com.fyo.domain.User;
import com.fyo.repository.UserRepository;
import com.fyo.team.dto.UserSummaryResponse;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    /** Below this a search would return most of the table, which is noise, not a result. */
    private static final int MIN_TERM_LENGTH = 2;
    private static final int MAX_RESULTS = 10;

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserSummaryResponse> search(String term, int limit) {
        String trimmed = term == null ? "" : term.trim();
        if (trimmed.length() < MIN_TERM_LENGTH) {
            return List.of();
        }

        int capped = Math.min(Math.max(limit, 1), MAX_RESULTS);
        String pattern = "%" + escapeLike(trimmed.toLowerCase()) + "%";

        return userRepository.searchActive(pattern, PageRequest.of(0, capped)).stream()
                .map(UserService::toSummary)
                .toList();
    }

    /** Escapes the SQL LIKE metacharacters, using `!` as the escape character. */
    private static String escapeLike(String term) {
        return term.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    private static UserSummaryResponse toSummary(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getSurname(),
                user.getRegion(),
                user.getImageUrl()
        );
    }
}
