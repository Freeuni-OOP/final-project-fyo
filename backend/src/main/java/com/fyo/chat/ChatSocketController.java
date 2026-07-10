package com.fyo.chat;

import com.fyo.chat.dto.ChatMessageResponse;
import com.fyo.chat.dto.SocketChatMessagePayload;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * STOMP entry point for chat. NOTE: the handshake is not authenticated yet,
 * so the sender id in the payload is still client-asserted. A CONNECT
 * interceptor (planned) will replace it with a session principal; the REST
 * endpoints are the trusted path until then.
 */
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
            @Payload SocketChatMessagePayload payload
    ) {
        ChatMessageResponse saved = chatService.sendMessage(
                conversationId, payload.senderUserId(), payload.body());
        messagingTemplate.convertAndSend("/topic/conversations/" + conversationId, saved);
    }
}
