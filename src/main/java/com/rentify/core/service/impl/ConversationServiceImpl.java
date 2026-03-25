package com.rentify.core.service.impl;

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
import com.rentify.core.service.ConversationService;
import com.rentify.core.validation.ConversationValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final PropertyRepository propertyRepository;
    private final AuthenticationService authService;
    private final ChatMapper chatMapper;
    private final ConversationValidator conversationValidator;

    @Override
    @Transactional
    public ConversationDto getOrCreateConversation(Long propertyId) {
        conversationValidator.validatePropertyId(propertyId);

        User currentUser = authService.getCurrentUser();
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));
        conversationValidator.validateConversationInitiator(property, currentUser);

        Conversation conversation = findOrCreateConversation(property, currentUser);
        return chatMapper.toConversationDto(conversation);
    }

    @Override
    @Transactional
    public MessageDto sendMessageToProperty(Long propertyId, SendMessageRequestDto request) {
        ConversationDto conversation = getOrCreateConversation(propertyId);
        return sendMessage(conversation.id(), request);
    }

    @Override
    @Transactional
    public MessageDto sendMessage(Long conversationId, SendMessageRequestDto request) {
        conversationValidator.validateSendMessageRequest(request);

        User sender = authService.getCurrentUser();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));
        conversationValidator.validateParticipant(conversation, sender);

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .type(MessageType.TEXT)
                .text(request.text())
                .isRead(false)
                .build();
        return chatMapper.toMessageDto(messageRepository.save(message));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationDto> getMyConversations() {
        User currentUser = authService.getCurrentUser();
        return chatMapper.toConversationDtos(conversationRepository.findAllByUserId(currentUser.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageDto> getConversationMessages(Long conversationId) {
        User currentUser = authService.getCurrentUser();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));
        conversationValidator.validateParticipant(conversation, currentUser);
        return chatMapper.toMessageDtos(messageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId));
    }

    private Conversation findOrCreateConversation(Property property, User tenant) {
        return conversationRepository.findByPropertyIdAndTenantId(property.getId(), tenant.getId())
                .orElseGet(() -> {
                    Conversation newConversation = Conversation.builder()
                            .property(property)
                            .host(property.getHost())
                            .tenant(tenant)
                            .build();
                    try {
                        return conversationRepository.save(newConversation);
                    } catch (DataIntegrityViolationException ex) {
                        return conversationRepository.findByPropertyIdAndTenantId(property.getId(), tenant.getId())
                                .orElseThrow(() -> ex);
                    }
                });
    }
}
