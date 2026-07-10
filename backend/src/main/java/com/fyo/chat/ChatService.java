package com.fyo.chat;

import com.fyo.chat.dto.ChatMessageResponse;
import com.fyo.chat.dto.ConversationParticipantResponse;
import com.fyo.chat.dto.ConversationResponse;
import com.fyo.chat.dto.CreateDirectConversationRequest;
import com.fyo.domain.ChatMessage;
import com.fyo.domain.Conversation;
import com.fyo.domain.ConversationParticipant;
import com.fyo.domain.User;
import com.fyo.repository.ChatMessageRepository;
import com.fyo.repository.ConversationParticipantRepository;
import com.fyo.repository.ConversationRepository;
import com.fyo.repository.UserRepository;
import java.util.List;
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

    public ChatService(
            ConversationRepository conversationRepository,
            ConversationParticipantRepository participantRepository,
            ChatMessageRepository messageRepository,
            UserRepository userRepository
    ) {
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
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

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(Long conversationId, Long userId) {
        ensureConversationExists(conversationId);
        ensureParticipant(conversationId, userId);
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
                .map(this::toMessageResponse)
                .toList();
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

    @Transactional
    public ConversationResponse createDirectConversation(Long currentUserId, CreateDirectConversationRequest request) {
        if (currentUserId.equals(request.otherUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conversation needs two different users");
        }

        if (request.matchId() != null) {
            return conversationRepository.findByMatchId(request.matchId())
                    .map(this::toConversationResponse)
                    .orElseGet(() -> createConversation(currentUserId, request));
        }

        return createConversation(currentUserId, request);
    }

    private ConversationResponse createConversation(Long currentUserId, CreateDirectConversationRequest request) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Current user not found"));
        User otherUser = userRepository.findById(request.otherUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Other user not found"));

        Conversation conversation = conversationRepository.save(new Conversation(request.matchId()));
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
                conversation.getMatchId(),
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
