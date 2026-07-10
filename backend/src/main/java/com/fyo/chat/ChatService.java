package com.fyo.chat;

import com.fyo.chat.dto.ChatMessageResponse;
import com.fyo.chat.dto.ConversationParticipantResponse;
import com.fyo.chat.dto.ConversationResponse;
import com.fyo.chat.dto.CreateDirectConversationRequest;
import com.fyo.domain.ChatMessage;
import com.fyo.domain.Conversation;
import com.fyo.domain.ConversationParticipant;
import com.fyo.domain.Match;
import com.fyo.domain.MatchFormat;
import com.fyo.domain.Team;
import com.fyo.domain.TeamMember;
import com.fyo.domain.User;
import com.fyo.repository.ChatMessageRepository;
import com.fyo.repository.ConversationParticipantRepository;
import com.fyo.repository.ConversationRepository;
import com.fyo.repository.TeamMemberRepository;
import com.fyo.repository.UserRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;

    public ChatService(
            ConversationRepository conversationRepository,
            ConversationParticipantRepository participantRepository,
            ChatMessageRepository messageRepository,
            UserRepository userRepository,
            TeamMemberRepository teamMemberRepository
    ) {
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversationsForUser(Long userId) {
        ensureUserExists(userId);
        return participantRepository.findByUserId(userId).stream()
                .map(ConversationParticipant::getConversation)
                .distinct()
                .map(this::toConversationResponse)
                .toList();
    }

    private static final int MAX_MESSAGE_PAGE = 100;

    @Transactional(readOnly = true)
    public ConversationResponse getConversationByMatch(Long matchId, Long userId) {
        Conversation conversation = conversationRepository.findByMatchId(matchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No conversation for this match"));
        ensureParticipant(conversation.getId(), userId);
        return toConversationResponse(conversation);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(Long conversationId, Long userId, Long beforeMessageId, int limit) {
        ensureConversationExists(conversationId);
        ensureParticipant(conversationId, userId);

        int pageSize = Math.clamp(limit, 1, MAX_MESSAGE_PAGE);
        List<ChatMessage> page;

        if (beforeMessageId == null) {
            page = new ArrayList<>(messageRepository.findByConversationIdOrderByCreatedAtDesc(
                    conversationId, PageRequest.of(0, pageSize)));
            Collections.reverse(page);
        } else {
            ChatMessage cursor = messageRepository.findById(beforeMessageId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found"));
            if (!cursor.getConversation().getId().equals(conversationId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message does not belong to this conversation");
            }
            page = new ArrayList<>(messageRepository.findByConversationIdAndCreatedAtLessThanOrderByCreatedAtDesc(
                    conversationId, cursor.getCreatedAt(), PageRequest.of(0, pageSize)));
            Collections.reverse(page);
        }

        return page.stream().map(this::toMessageResponse).toList();
    }

    /**
     * Persists a message from {@code senderUserId}, which the caller must have
     * resolved from a trusted source (Bearer token on REST; socket principal
     * once WebSocket auth lands).
     */
    @Transactional
    public ChatMessageResponse sendMessage(Long conversationId, Long senderUserId, String rawBody) {
        Conversation conversation = ensureConversationExists(conversationId);
        User sender = userRepository.findById(senderUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender user not found"));
        ensureParticipant(conversationId, sender.getId());

        String body = rawBody == null ? "" : rawBody.trim();
        if (body.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message body cannot be blank");
        }

        ChatMessage saved = messageRepository.save(new ChatMessage(conversation, sender, body));
        return toMessageResponse(saved);
    }

    /** Creates (or returns the existing) 1:1 thread between the caller and another user. */
    @Transactional
    public ConversationResponse createDirectConversation(Long currentUserId, CreateDirectConversationRequest request) {
        if (currentUserId.equals(request.otherUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conversation needs two different users");
        }

        return conversationRepository.findDirectBetween(currentUserId, request.otherUserId())
                .map(this::toConversationResponse)
                .orElseGet(() -> createDirect(currentUserId, request.otherUserId()));
    }

    /**
     * Creates (or returns the existing) group chat for a team roster. Only
     * members of the team may open it; every current member joins as a
     * participant. Idempotent per team (conversations.team_id is UNIQUE).
     */
    @Transactional
    public ConversationResponse createTeamConversation(Long currentUserId, Long teamId) {
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only team members can open the team chat");
        }

        return conversationRepository.findByTeamId(teamId)
                .map(this::toConversationResponse)
                .orElseGet(() -> {
                    Conversation conversation = conversationRepository.save(Conversation.forTeam(teamId));
                    for (TeamMember member : teamMemberRepository.findByTeamId(teamId)) {
                        participantRepository.save(new ConversationParticipant(conversation, member.getUser()));
                    }
                    return toConversationResponse(conversation);
                });
    }

    /**
     * Keeps the team chat in sync with the roster: called when a user joins a
     * team, so an existing team conversation picks them up. No-op when the
     * team has no chat yet or the user is already in it.
     */
    @Transactional
    public void addUserToTeamConversation(Long teamId, User user) {
        conversationRepository.findByTeamId(teamId).ifPresent(conversation -> {
            if (!participantRepository.existsByConversationIdAndUserId(conversation.getId(), user.getId())) {
                participantRepository.save(new ConversationParticipant(conversation, user));
            }
        });
    }

    /**
     * Creates the conversation for a freshly confirmed match, so both sides can
     * agree on time, place, and rules. Idempotent per match (conversations.match_id
     * is UNIQUE). ONE_VS_ONE gets the two players; TEAM_VS_TEAM gets every member
     * of both rosters as a group chat.
     *
     * <p>Internal API — called by the match acceptance flow, never from a
     * controller with client-supplied ids.
     */
    @Transactional
    public ConversationResponse createMatchConversation(Match match) {
        return conversationRepository.findByMatchId(match.getId())
                .map(this::toConversationResponse)
                .orElseGet(() -> {
                    Conversation conversation = conversationRepository.save(Conversation.forMatch(match.getId()));
                    for (User participant : matchParticipants(match)) {
                        participantRepository.save(new ConversationParticipant(conversation, participant));
                    }
                    return toConversationResponse(conversation);
                });
    }

    private Collection<User> matchParticipants(Match match) {
        // Keyed by user id so a player on both rosters is added only once.
        Map<Long, User> participants = new LinkedHashMap<>();
        if (match.getFormat() == MatchFormat.ONE_VS_ONE) {
            participants.put(match.getHomeUser().getId(), match.getHomeUser());
            participants.put(match.getAwayUser().getId(), match.getAwayUser());
        } else {
            addRoster(participants, match.getHomeTeam());
            addRoster(participants, match.getAwayTeam());
        }
        return participants.values();
    }

    private void addRoster(Map<Long, User> participants, Team team) {
        for (TeamMember member : teamMemberRepository.findByTeamId(team.getId())) {
            participants.put(member.getUser().getId(), member.getUser());
        }
    }

    private ConversationResponse createDirect(Long currentUserId, Long otherUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Current user not found"));
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Other user not found"));

        Conversation conversation = conversationRepository.save(Conversation.direct());
        participantRepository.save(new ConversationParticipant(conversation, currentUser));
        participantRepository.save(new ConversationParticipant(conversation, otherUser));

        return toConversationResponse(conversation);
    }

    private User ensureUserExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Conversation ensureConversationExists(Long conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
    }

    private void ensureParticipant(Long conversationId, Long userId) {
        if (!participantRepository.existsByConversationIdAndUserId(conversationId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a conversation participant");
        }
    }

    private ConversationResponse toConversationResponse(Conversation conversation) {
        List<ConversationParticipantResponse> participants = participantRepository
                .findByConversationId(conversation.getId())
                .stream()
                .map(this::toParticipantResponse)
                .toList();
        ChatMessageResponse lastMessage = messageRepository
                .findFirstByConversationIdOrderByCreatedAtDesc(conversation.getId())
                .map(this::toMessageResponse)
                .orElse(null);

        return new ConversationResponse(
                conversation.getId(),
                conversation.getType(),
                conversation.getMatchId(),
                conversation.getTeamId(),
                participants,
                lastMessage,
                conversation.getCreatedAt()
        );
    }

    private ConversationParticipantResponse toParticipantResponse(ConversationParticipant participant) {
        User user = participant.getUser();
        return new ConversationParticipantResponse(
                user.getId(),
                user.getUsername(),
                user.getName() + " " + user.getSurname(),
                user.getImageUrl()
        );
    }

    private ChatMessageResponse toMessageResponse(ChatMessage message) {
        User sender = message.getSender();
        return new ChatMessageResponse(
                message.getId(),
                message.getConversation().getId(),
                sender.getId(),
                sender.getUsername(),
                message.getBody(),
                message.getCreatedAt(),
                message.getReadAt()
        );
    }
}
