package com.fyo.chat;

import com.fyo.chat.dto.ChatMessageResponse;
import com.fyo.chat.dto.ConversationResponse;
import com.fyo.chat.dto.CreateDirectConversationRequest;
import com.fyo.chat.dto.SendChatMessageRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversations")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public List<ConversationResponse> getConversations(@RequestParam Long userId) {
        return chatService.getConversationsForUser(userId);
    }

    @PostMapping("/direct")
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationResponse createDirectConversation(@Valid @RequestBody CreateDirectConversationRequest request) {
        return chatService.createDirectConversation(request);
    }

    @GetMapping("/{conversationId}/messages")
    public List<ChatMessageResponse> getMessages(@PathVariable Long conversationId, @RequestParam Long userId) {
        return chatService.getMessages(conversationId, userId);
    }

    @PostMapping("/{conversationId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatMessageResponse sendMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody SendChatMessageRequest request
    ) {
        return chatService.sendMessage(conversationId, request);
    }
}
