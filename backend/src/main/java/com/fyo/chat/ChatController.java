package com.fyo.chat;

import com.fyo.auth.CurrentUserService;
import com.fyo.chat.dto.ChatMessageResponse;
import com.fyo.chat.dto.ConversationResponse;
import com.fyo.chat.dto.CreateDirectConversationRequest;
import com.fyo.chat.dto.SendChatMessageRequest;
import com.fyo.domain.User;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Chat REST API. Every endpoint resolves the caller from the Authorization
 * Bearer token — client-supplied user ids are never trusted for identity.
 */
@RestController
@RequestMapping("/api/conversations")
public class ChatController {

    private final ChatService chatService;
    private final CurrentUserService currentUserService;

    public ChatController(ChatService chatService, CurrentUserService currentUserService) {
        this.chatService = chatService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<ConversationResponse> getConversations(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        return chatService.getConversationsForUser(currentUser.getId());
    }

    @PostMapping("/direct")
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationResponse createDirectConversation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody CreateDirectConversationRequest request
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        return chatService.createDirectConversation(currentUser.getId(), request);
    }

    @GetMapping("/{conversationId}/messages")
    public List<ChatMessageResponse> getMessages(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long conversationId
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        return chatService.getMessages(conversationId, currentUser.getId());
    }

    @PostMapping("/{conversationId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatMessageResponse sendMessage(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long conversationId,
            @Valid @RequestBody SendChatMessageRequest request
    ) {
        User currentUser = currentUserService.requireCurrentUser(authorization);
        return chatService.sendMessage(conversationId, currentUser.getId(), request.body());
    }
}
