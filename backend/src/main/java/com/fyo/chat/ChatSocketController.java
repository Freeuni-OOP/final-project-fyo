package com.fyo.chat;

import com.fyo.chat.dto.ChatMessageResponse;
import com.fyo.chat.dto.SendChatMessageRequest;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatSocketController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/conversations/{conversationId}/send")
    public void sendMessage(
            @DestinationVariable Long conversationId,
            @Payload SendChatMessageRequest request
    ) {
        ChatMessageResponse saved = chatService.sendMessage(conversationId, request);
        messagingTemplate.convertAndSend("/topic/conversations/" + conversationId, saved);
    }
}
