package com.rentify.core.validation;

import com.rentify.core.dto.conversation.SendMessageRequestDto;
import com.rentify.core.entity.Conversation;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.User;
import jakarta.validation.Validator;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class ConversationValidator extends AbstractValidator {

    public ConversationValidator(Validator validator) {
        super(validator);
    }

    public void validatePropertyId(Long propertyId) {
        if (propertyId == null || propertyId <= 0) {
            throw new IllegalArgumentException("Property id must be positive");
        }
    }

    public void validateConversationInitiator(Property property, User currentUser) {
        if (property.getHost().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Host cannot initiate a conversation for their own property");
        }
    }

    public void validateSendMessageRequest(SendMessageRequestDto request) {
        throwIfAny(collectBeanErrors(request));
    }

    public void validateParticipant(Conversation conversation, User user) {
        if (!conversation.getHost().getId().equals(user.getId())
                && !conversation.getTenant().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to access this conversation");
        }
    }
}
