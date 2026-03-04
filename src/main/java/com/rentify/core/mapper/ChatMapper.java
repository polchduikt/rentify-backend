package com.rentify.core.mapper;

import com.rentify.core.dto.conversation.ConversationDto;
import com.rentify.core.dto.conversation.MessageDto;
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
    @Mapping(source = "isRead", target = "isRead")
    MessageDto toMessageDto(Message message);
}
