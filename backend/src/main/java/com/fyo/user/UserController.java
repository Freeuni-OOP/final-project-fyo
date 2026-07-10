package com.fyo.user;

import com.fyo.team.dto.UserSummaryResponse;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Typeahead for player pickers. A short or blank term returns nothing. */
    @GetMapping
    public List<UserSummaryResponse> search(
            @RequestParam(name = "q", defaultValue = "") String q,
            @RequestParam(name = "limit", defaultValue = "8") int limit
    ) {
        return userService.search(q, limit);
    }
}
