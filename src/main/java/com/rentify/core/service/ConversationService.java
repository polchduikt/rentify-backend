package com.rentify.core.service;

import com.rentify.core.dto.conversation.ConversationDto;
import com.rentify.core.dto.conversation.MessageDto;
import com.rentify.core.dto.conversation.SendMessageRequestDto;

import java.util.List;

public interface ConversationService {
    ConversationDto getOrCreateConversation(Long propertyId);
    default ConversationDto createConversation(Long propertyId) {
        return getOrCreateConversation(propertyId);
    }
    MessageDto sendMessageToProperty(Long propertyId, SendMessageRequestDto request);
    MessageDto sendMessage(Long conversationId, SendMessageRequestDto request);
    List<ConversationDto> getMyConversations();
    List<MessageDto> getConversationMessages(Long conversationId);
}
