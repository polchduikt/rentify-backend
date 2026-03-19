package com.rentify.core.unit;

import com.rentify.core.dto.conversation.ConversationDto;
import com.rentify.core.dto.conversation.MessageDto;
import com.rentify.core.dto.conversation.SendMessageRequestDto;
import com.rentify.core.entity.Conversation;
import com.rentify.core.entity.Message;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.User;
import com.rentify.core.enums.MessageType;
import com.rentify.core.mapper.ChatMapper;
import com.rentify.core.repository.ConversationRepository;
import com.rentify.core.repository.MessageRepository;
import com.rentify.core.repository.PropertyRepository;
import com.rentify.core.service.AuthenticationService;
import com.rentify.core.service.impl.ConversationServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationServiceImplTest {

    @Mock private ConversationRepository conversationRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private PropertyRepository propertyRepository;
    @Mock private AuthenticationService authService;
    @Mock private ChatMapper chatMapper;

    @InjectMocks
    private ConversationServiceImpl conversationService;

    private User host;
    private User tenant;
    private User outsider;
    private Property property;
    private Conversation conversation;
    private SendMessageRequestDto sendRequest;

    @BeforeEach
    void setUp() {
        host = User.builder().id(1L).build();
        tenant = User.builder().id(2L).build();
        outsider = User.builder().id(3L).build();

        property = Property.builder().id(10L).host(host).build();

        conversation = Conversation.builder()
                .id(100L)
                .property(property)
                .host(host)
                .tenant(tenant)
                .build();

        sendRequest = new SendMessageRequestDto("Hello!");
    }

    @Nested
    @DisplayName("sendMessage()")
    class SendMessageTests {

        @Test
        void shouldThrowEntityNotFound_whenPropertyMissing() {
            when(authService.getCurrentUser()).thenReturn(tenant);
            when(propertyRepository.findById(10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> conversationService.sendMessage(10L, sendRequest))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Property not found");
        }

        @Test
        void shouldThrowIllegalArgument_whenHostStartsConversationOnOwnProperty() {
            when(authService.getCurrentUser()).thenReturn(host);
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));

            assertThatThrownBy(() -> conversationService.sendMessage(10L, sendRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Host cannot initiate a conversation for their own property");
        }

        @Test
        void shouldSendMessageInExistingConversation() {
            Message savedMessage = Message.builder()
                    .id(200L)
                    .conversation(conversation)
                    .sender(tenant)
                    .type(MessageType.TEXT)
                    .text("Hello!")
                    .isRead(false)
                    .build();
            MessageDto dto = new MessageDto(
                    200L, 100L, 2L, MessageType.TEXT, "Hello!", false, null, ZonedDateTime.now()
            );

            when(authService.getCurrentUser()).thenReturn(tenant);
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
            when(conversationRepository.findByPropertyIdAndTenantId(10L, 2L)).thenReturn(Optional.of(conversation));
            when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
            when(chatMapper.toMessageDto(savedMessage)).thenReturn(dto);

            MessageDto result = conversationService.sendMessage(10L, sendRequest);

            assertThat(result.id()).isEqualTo(200L);
            verify(conversationRepository, never()).save(any(Conversation.class));
        }

        @Test
        void shouldCreateConversationAndSendMessage_whenConversationMissing() {
            Conversation savedConversation = Conversation.builder()
                    .id(101L)
                    .property(property)
                    .host(host)
                    .tenant(tenant)
                    .build();
            Message savedMessage = Message.builder()
                    .id(201L)
                    .conversation(savedConversation)
                    .sender(tenant)
                    .type(MessageType.TEXT)
                    .text("Hello!")
                    .isRead(false)
                    .build();
            MessageDto dto = new MessageDto(
                    201L, 101L, 2L, MessageType.TEXT, "Hello!", false, null, ZonedDateTime.now()
            );

            when(authService.getCurrentUser()).thenReturn(tenant);
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
            when(conversationRepository.findByPropertyIdAndTenantId(10L, 2L)).thenReturn(Optional.empty());
            when(conversationRepository.save(any(Conversation.class))).thenReturn(savedConversation);
            when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
            when(chatMapper.toMessageDto(savedMessage)).thenReturn(dto);

            MessageDto result = conversationService.sendMessage(10L, sendRequest);
            ArgumentCaptor<Conversation> conversationCaptor = ArgumentCaptor.forClass(Conversation.class);

            assertThat(result.id()).isEqualTo(201L);
            verify(conversationRepository).save(conversationCaptor.capture());
            assertThat(conversationCaptor.getValue().getHost()).isEqualTo(host);
            assertThat(conversationCaptor.getValue().getTenant()).isEqualTo(tenant);
        }
    }

    @Nested
    @DisplayName("replyToConversation()")
    class ReplyToConversationTests {

        @Test
        void shouldThrowEntityNotFound_whenConversationMissing() {
            when(authService.getCurrentUser()).thenReturn(tenant);
            when(conversationRepository.findById(100L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> conversationService.replyToConversation(100L, sendRequest))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Conversation not found");
        }

        @Test
        void shouldThrowAccessDenied_whenSenderIsNotParticipant() {
            when(authService.getCurrentUser()).thenReturn(outsider);
            when(conversationRepository.findById(100L)).thenReturn(Optional.of(conversation));

            assertThatThrownBy(() -> conversationService.replyToConversation(100L, sendRequest))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("You do not have permission to access this conversation");
        }

        @Test
        void shouldReplyToConversation_whenSenderIsParticipant() {
            Message savedMessage = Message.builder()
                    .id(300L)
                    .conversation(conversation)
                    .sender(host)
                    .type(MessageType.TEXT)
                    .text("Reply")
                    .isRead(false)
                    .build();
            MessageDto dto = new MessageDto(
                    300L, 100L, 1L, MessageType.TEXT, "Reply", false, null, ZonedDateTime.now()
            );

            when(authService.getCurrentUser()).thenReturn(host);
            when(conversationRepository.findById(100L)).thenReturn(Optional.of(conversation));
            when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
            when(chatMapper.toMessageDto(savedMessage)).thenReturn(dto);

            MessageDto result = conversationService.replyToConversation(100L, new SendMessageRequestDto("Reply"));

            assertThat(result.id()).isEqualTo(300L);
        }
    }

    @Nested
    @DisplayName("getMyConversations()")
    class GetMyConversationsTests {

        @Test
        void shouldReturnMappedConversations() {
            ConversationDto dto = new ConversationDto(100L, 10L, 1L, 2L, ZonedDateTime.now());
            when(authService.getCurrentUser()).thenReturn(tenant);
            when(conversationRepository.findAllByUserId(2L)).thenReturn(List.of(conversation));
            when(chatMapper.toConversationDto(conversation)).thenReturn(dto);

            List<ConversationDto> result = conversationService.getMyConversations();

            assertThat(result).containsExactly(dto);
            verify(conversationRepository).findAllByUserId(2L);
        }
    }

    @Nested
    @DisplayName("getConversationMessages()")
    class GetConversationMessagesTests {

        @Test
        void shouldThrowAccessDenied_whenUserIsNotParticipant() {
            when(authService.getCurrentUser()).thenReturn(outsider);
            when(conversationRepository.findById(100L)).thenReturn(Optional.of(conversation));

            assertThatThrownBy(() -> conversationService.getConversationMessages(100L))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("You do not have permission to access this conversation");
            verify(messageRepository, never()).findAllByConversationIdOrderByCreatedAtAsc(any());
        }

        @Test
        void shouldReturnMappedMessages_whenUserIsParticipant() {
            Message message = Message.builder()
                    .id(401L)
                    .conversation(conversation)
                    .sender(tenant)
                    .type(MessageType.TEXT)
                    .text("Ping")
                    .isRead(false)
                    .build();
            MessageDto dto = new MessageDto(
                    401L, 100L, 2L, MessageType.TEXT, "Ping", false, null, ZonedDateTime.now()
            );

            when(authService.getCurrentUser()).thenReturn(tenant);
            when(conversationRepository.findById(100L)).thenReturn(Optional.of(conversation));
            when(messageRepository.findAllByConversationIdOrderByCreatedAtAsc(100L)).thenReturn(List.of(message));
            when(chatMapper.toMessageDto(eq(message))).thenReturn(dto);

            List<MessageDto> result = conversationService.getConversationMessages(100L);

            assertThat(result).containsExactly(dto);
        }
    }
}
