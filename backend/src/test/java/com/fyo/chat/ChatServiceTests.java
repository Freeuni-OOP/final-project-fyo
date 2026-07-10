package com.fyo.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fyo.chat.dto.ChatMessageResponse;
import com.fyo.chat.dto.ConversationResponse;
import com.fyo.chat.dto.CreateDirectConversationRequest;
import com.fyo.domain.ConversationType;
import com.fyo.domain.Sport;
import com.fyo.domain.Team;
import com.fyo.domain.TeamMember;
import com.fyo.domain.TeamMemberRole;
import com.fyo.domain.User;
import com.fyo.repository.SportRepository;
import com.fyo.repository.TeamMemberRepository;
import com.fyo.repository.TeamRepository;
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

    @Autowired
    private SportRepository sportRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Test
    void createDirectConversationAddsBothParticipants() {
        List<User> users = userRepository.findAll();
        User userA = users.get(0);
        User userB = users.get(1);

        ConversationResponse conversation = chatService.createDirectConversation(
                userA.getId(),
                new CreateDirectConversationRequest(userB.getId())
        );

        assertThat(conversation.id()).isNotNull();
        assertThat(conversation.participants())
                .extracting(participant -> participant.userId())
                .containsExactlyInAnyOrder(userA.getId(), userB.getId());
    }

    @Test
    void createDirectConversationReusesExistingThread() {
        List<User> users = userRepository.findAll();
        User userA = users.get(0);
        User userB = users.get(1);

        ConversationResponse first = chatService.createDirectConversation(
                userA.getId(), new CreateDirectConversationRequest(userB.getId()));
        // Same pair from the other side must land in the same thread.
        ConversationResponse second = chatService.createDirectConversation(
                userB.getId(), new CreateDirectConversationRequest(userA.getId()));

        assertThat(second.id()).isEqualTo(first.id());
    }

    @Test
    void createTeamConversationAddsWholeRosterAndIsIdempotent() {
        List<User> users = userRepository.findAll();
        User captain = users.get(0);
        User member = users.get(1);
        Sport sport = sportRepository.findAll().getFirst();

        Team team = teamRepository.save(new Team(
                "Chat FC", sport, "Tbilisi", null, null, captain, (short) 5, (short) 3, true));
        teamMemberRepository.save(new TeamMember(team, captain, TeamMemberRole.CAPTAIN));
        teamMemberRepository.save(new TeamMember(team, member, TeamMemberRole.MEMBER));

        ConversationResponse conversation = chatService.createTeamConversation(member.getId(), team.getId());

        assertThat(conversation.type()).isEqualTo(ConversationType.TEAM);
        assertThat(conversation.teamId()).isEqualTo(team.getId());
        assertThat(conversation.participants())
                .extracting(participant -> participant.userId())
                .containsExactlyInAnyOrder(captain.getId(), member.getId());

        ConversationResponse again = chatService.createTeamConversation(captain.getId(), team.getId());
        assertThat(again.id()).isEqualTo(conversation.id());
    }

    @Test
    void createTeamConversationRejectsNonMember() {
        List<User> users = userRepository.findAll();
        User captain = users.get(0);
        User outsider = users.get(1);
        Sport sport = sportRepository.findAll().getFirst();

        Team team = teamRepository.save(new Team(
                "Members Only FC", sport, "Tbilisi", null, null, captain, (short) 5, (short) 4, true));
        teamMemberRepository.save(new TeamMember(team, captain, TeamMemberRole.CAPTAIN));

        assertThatThrownBy(() -> chatService.createTeamConversation(outsider.getId(), team.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only team members");
    }

    @Test
    void addUserToTeamConversationSyncsRosterIntoExistingChat() {
        List<User> users = userRepository.findAll();
        User captain = users.get(0);
        User newcomer = users.get(1);
        Sport sport = sportRepository.findAll().getFirst();

        Team team = teamRepository.save(new Team(
                "Growing FC", sport, "Tbilisi", null, null, captain, (short) 5, (short) 4, true));
        teamMemberRepository.save(new TeamMember(team, captain, TeamMemberRole.CAPTAIN));
        ConversationResponse conversation = chatService.createTeamConversation(captain.getId(), team.getId());

        chatService.addUserToTeamConversation(team.getId(), newcomer);
        // Second call must not duplicate the participant row.
        chatService.addUserToTeamConversation(team.getId(), newcomer);

        assertThat(chatService.getMessages(conversation.id(), newcomer.getId())).isEmpty();
    }

    @Test
    void createDirectConversationRejectsSelf() {
        User userA = userRepository.findAll().get(0);

        assertThatThrownBy(() -> chatService.createDirectConversation(
                userA.getId(),
                new CreateDirectConversationRequest(userA.getId())
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Conversation needs two different users");
    }

    @Test
    void sendMessageSavesMessageForParticipant() {
        List<User> users = userRepository.findAll();
        User sender = users.get(0);
        User receiver = users.get(1);
        ConversationResponse conversation = chatService.createDirectConversation(
                sender.getId(),
                new CreateDirectConversationRequest(receiver.getId())
        );

        ChatMessageResponse sent = chatService.sendMessage(
                conversation.id(), sender.getId(), "  hello from chat  ");

        assertThat(sent.id()).isNotNull();
        assertThat(sent.senderId()).isEqualTo(sender.getId());
        assertThat(sent.body()).isEqualTo("hello from chat");
        // The thread may be a reused existing conversation with prior history,
        // so assert on the newest message rather than the whole list.
        assertThat(chatService.getMessages(conversation.id(), receiver.getId()))
                .extracting(message -> message.body())
                .endsWith("hello from chat");
    }

    @Test
    void sendMessageRejectsNonParticipant() {
        List<User> users = userRepository.findAll();
        User userA = users.get(0);
        User userB = users.get(1);
        User outsider = users.get(2);
        ConversationResponse conversation = chatService.createDirectConversation(
                userA.getId(),
                new CreateDirectConversationRequest(userB.getId())
        );

        assertThatThrownBy(() -> chatService.sendMessage(
                conversation.id(), outsider.getId(), "not allowed"))
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
                userA.getId(),
                new CreateDirectConversationRequest(userB.getId())
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
                userA.getId(),
                new CreateDirectConversationRequest(userB.getId())
        );

        assertThatThrownBy(() -> chatService.sendMessage(conversation.id(), userA.getId(), "   "))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Message body cannot be blank");
    }
}
