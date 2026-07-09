package com.fyo.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fyo.chat.dto.ChatMessageResponse;
import com.fyo.chat.dto.ConversationResponse;
import com.fyo.chat.dto.CreateDirectConversationRequest;
import com.fyo.chat.dto.SendChatMessageRequest;
import com.fyo.domain.User;
import com.fyo.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@Transactional
class ChatServiceTests {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createDirectConversationAddsBothParticipants() {
        List<User> users = userRepository.findAll();
        User userA = users.get(0);
        User userB = users.get(1);

        ConversationResponse conversation = chatService.createDirectConversation(
                new CreateDirectConversationRequest(userA.getId(), userB.getId(), null)
        );

        assertThat(conversation.id()).isNotNull();
        assertThat(conversation.participants())
                .extracting(participant -> participant.userId())
                .containsExactlyInAnyOrder(userA.getId(), userB.getId());
    }

    @Test
    void sendMessageSavesMessageForParticipant() {
        List<User> users = userRepository.findAll();
        User sender = users.get(0);
        User receiver = users.get(1);
        ConversationResponse conversation = chatService.createDirectConversation(
                new CreateDirectConversationRequest(sender.getId(), receiver.getId(), null)
        );

        ChatMessageResponse sent = chatService.sendMessage(
                conversation.id(),
                new SendChatMessageRequest(sender.getId(), "  hello from chat  ")
        );

        assertThat(sent.id()).isNotNull();
        assertThat(sent.senderId()).isEqualTo(sender.getId());
        assertThat(sent.body()).isEqualTo("hello from chat");
        assertThat(chatService.getMessages(conversation.id(), receiver.getId()))
                .extracting(message -> message.body())
                .containsExactly("hello from chat");
    }

    @Test
    void sendMessageRejectsNonParticipant() {
        List<User> users = userRepository.findAll();
        User userA = users.get(0);
        User userB = users.get(1);
        User outsider = users.get(2);
        ConversationResponse conversation = chatService.createDirectConversation(
                new CreateDirectConversationRequest(userA.getId(), userB.getId(), null)
        );

        assertThatThrownBy(() -> chatService.sendMessage(
                conversation.id(),
                new SendChatMessageRequest(outsider.getId(), "not allowed")
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User is not a conversation participant");
    }

    @Test
    void getMessagesRejectsNonParticipant() {
        List<User> users = userRepository.findAll();
        User userA = users.get(0);
        User userB = users.get(1);
        User outsider = users.get(2);
        ConversationResponse conversation = chatService.createDirectConversation(
                new CreateDirectConversationRequest(userA.getId(), userB.getId(), null)
        );

        assertThatThrownBy(() -> chatService.getMessages(conversation.id(), outsider.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User is not a conversation participant");
    }

    @Test
    void sendMessageRejectsBlankBody() {
        List<User> users = userRepository.findAll();
        User userA = users.get(0);
        User userB = users.get(1);
        ConversationResponse conversation = chatService.createDirectConversation(
                new CreateDirectConversationRequest(userA.getId(), userB.getId(), null)
        );

        assertThatThrownBy(() -> chatService.sendMessage(
                conversation.id(),
                new SendChatMessageRequest(userA.getId(), "   ")
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Message body cannot be blank");
    }
}
