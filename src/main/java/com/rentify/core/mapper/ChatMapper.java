package com.rentify.core.mapper;

import com.rentify.core.dto.ConversationDto;
import com.rentify.core.dto.MessageDto;
import com.rentify.core.entity.Conversation;
import com.rentify.core.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChatMapper {

    @Mapping(source = "property.id", target = "propertyId")
    @Mapping(source = "host.id", target = "hostId")
    @Mapping(source = "tenant.id", target = "tenantId")
    ConversationDto toConversationDto(Conversation conversation);
    @Mapping(source = "conversation.id", target = "conversationId")
    @Mapping(source = "sender.id", target = "senderId")
    MessageDto toMessageDto(Message message);
}