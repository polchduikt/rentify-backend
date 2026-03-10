package com.rentify.core.controller;

import com.rentify.core.dto.conversation.ConversationDto;
import com.rentify.core.dto.conversation.MessageDto;
import com.rentify.core.dto.conversation.SendMessageRequestDto;
import com.rentify.core.service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping("/property/{propertyId}")
    public ResponseEntity<MessageDto> sendMessage(
            @PathVariable Long propertyId,
            @Valid @RequestBody SendMessageRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(conversationService.sendMessage(propertyId, request));
    }

    @PostMapping("/{conversationId}/reply")
    public ResponseEntity<MessageDto> replyToConversation(
            @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(conversationService.replyToConversation(conversationId, request));
    }

    @GetMapping
    public ResponseEntity<List<ConversationDto>> getMyConversations() {
        return ResponseEntity.ok(conversationService.getMyConversations());
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<List<MessageDto>> getConversationMessages(
            @PathVariable Long conversationId) {
        return ResponseEntity.ok(conversationService.getConversationMessages(conversationId));
    }
}
