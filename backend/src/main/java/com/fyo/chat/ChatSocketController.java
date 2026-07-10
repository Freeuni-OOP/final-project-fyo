package com.fyo.chat;

import com.fyo.auth.AuthenticatedUserPrincipal;
import com.fyo.chat.dto.ChatMessageResponse;
import com.fyo.chat.dto.SocketChatMessagePayload;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

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
            @Payload SocketChatMessagePayload payload,
            Principal principal
    ) {
        Long senderUserId = requireUserId(principal);
        ChatMessageResponse saved = chatService.sendMessage(conversationId, senderUserId, payload.body());
        messagingTemplate.convertAndSend("/topic/conversations/" + conversationId, saved);
    }

    private static Long requireUserId(Principal principal) {
        if (principal instanceof AuthenticatedUserPrincipal authenticated) {
            return authenticated.userId();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "STOMP session is not authenticated");
    }
}
