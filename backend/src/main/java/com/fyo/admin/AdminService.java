package com.fyo.admin;

import com.fyo.admin.dto.CreateSportRequest;
import com.fyo.admin.dto.SportAdminResponse;
import com.fyo.admin.dto.TeamAdminResponse;
import com.fyo.admin.dto.UserAdminResponse;
import com.fyo.domain.Sport;
import com.fyo.domain.Team;
import com.fyo.domain.User;
import com.fyo.repository.SportRepository;
import com.fyo.repository.TeamRepository;
import com.fyo.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final SportRepository sportRepository;

    public AdminService(
            UserRepository userRepository,
            TeamRepository teamRepository,
            SportRepository sportRepository
    ) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.sportRepository = sportRepository;
    }

    private void verifyAdmin(Long adminUserId) {
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!admin.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not an admin");
        }
    }

    @Transactional(readOnly = true)
    public List<UserAdminResponse> getUsers(Long adminUserId) {
        verifyAdmin(adminUserId);
        return userRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(UserAdminResponse::from)
                .toList();
    }

    @Transactional
    public UserAdminResponse archiveUser(Long adminUserId, Long userId) {
        verifyAdmin(adminUserId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.isArchived()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already archived");
        }
        user.setArchived(true);
        user.setArchivedAt(Instant.now());
        return UserAdminResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<TeamAdminResponse> getTeams(Long adminUserId) {
        verifyAdmin(adminUserId);
        return teamRepository.findAll()
                .stream()
                .map(TeamAdminResponse::from)
                .toList();
    }

    @Transactional
    public TeamAdminResponse archiveTeam(Long adminUserId, Long teamId) {
        verifyAdmin(adminUserId);
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));
        if (team.isArchived()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Team is already archived");
        }
        team.setArchived(true);
        return TeamAdminResponse.from(teamRepository.save(team));
    }

    @Transactional(readOnly = true)
    public List<SportAdminResponse> getSports(Long adminUserId) {
        verifyAdmin(adminUserId);
        return sportRepository.findAll()
                .stream()
                .map(SportAdminResponse::from)
                .toList();
    }

    @Transactional
    public SportAdminResponse createSport(Long adminUserId, CreateSportRequest request) {
        verifyAdmin(adminUserId);
        Sport sport = new Sport(request.sportName().trim());
        return SportAdminResponse.from(sportRepository.save(sport));
    }

    @Transactional
    public void deleteSport(Long adminUserId, Long sportId) {
        verifyAdmin(adminUserId);
        Sport sport = sportRepository.findById(sportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sport not found"));
        sportRepository.delete(sport);
    }
}