package com.rentify.core.service;

import com.rentify.core.dto.conversation.ConversationDto;
import com.rentify.core.dto.conversation.MessageDto;
import com.rentify.core.dto.conversation.SendMessageRequestDto;

import java.util.List;

public interface ConversationService {
    ConversationDto createConversation(Long propertyId);
    MessageDto sendMessage(Long conversationId, SendMessageRequestDto request);
    List<ConversationDto> getMyConversations();
    List<MessageDto> getConversationMessages(Long conversationId);
}
