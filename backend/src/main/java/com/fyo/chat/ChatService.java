package com.fyo.chat;

import com.fyo.chat.dto.ChatMessageResponse;
import com.fyo.chat.dto.ConversationParticipantResponse;
import com.fyo.chat.dto.ConversationResponse;
import com.fyo.chat.dto.CreateDirectConversationRequest;
import com.fyo.chat.dto.SendChatMessageRequest;
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

    @Transactional
    public ChatMessageResponse sendMessage(Long conversationId, SendChatMessageRequest request) {
        Conversation conversation = ensureConversationExists(conversationId);
        User sender = userRepository.findById(request.senderUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender user not found"));
        ensureParticipant(conversationId, sender.getId());

        String body = request.body().trim();
        if (body.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message body cannot be blank");
        }

        ChatMessage saved = messageRepository.save(new ChatMessage(conversation, sender, body));
        return toMessageResponse(saved);
    }

    @Transactional
    public ConversationResponse createDirectConversation(CreateDirectConversationRequest request) {
        if (request.userAId().equals(request.userBId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conversation needs two different users");
        }

        if (request.matchId() != null) {
            return conversationRepository.findByMatchId(request.matchId())
                    .map(this::toConversationResponse)
                    .orElseGet(() -> createConversation(request));
        }

        return createConversation(request);
    }

    private ConversationResponse createConversation(CreateDirectConversationRequest request) {
        User userA = userRepository.findById(request.userAId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "First user not found"));
        User userB = userRepository.findById(request.userBId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Second user not found"));

        Conversation conversation = conversationRepository.save(new Conversation(request.matchId()));
        participantRepository.save(new ConversationParticipant(conversation, userA));
        participantRepository.save(new ConversationParticipant(conversation, userB));

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
