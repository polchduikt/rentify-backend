package com.rentify.core.controller;

import com.rentify.core.dto.conversation.ConversationDto;
import com.rentify.core.dto.conversation.MessageDto;
import com.rentify.core.dto.conversation.SendMessageRequestDto;
import com.rentify.core.service.ConversationService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversations", description = "Messaging between guests and hosts")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
})
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping("/property/{propertyId}")
    @Operation(
            summary = "Send first message for property",
            description = "Creates or reuses conversation for property and sends first message from authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Message sent",
                    content = @Content(schema = @Schema(implementation = MessageDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Property not found")
    })
    public ResponseEntity<MessageDto> sendMessage(
            @Parameter(description = "Property ID", example = "42")
            @PathVariable Long propertyId,
            @Valid @RequestBody SendMessageRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(conversationService.sendMessage(propertyId, request));
    }

    @PostMapping("/{conversationId}/reply")
    @Operation(
            summary = "Reply in existing conversation",
            description = "Sends reply message to an existing conversation available for authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Reply sent",
                    content = @Content(schema = @Schema(implementation = MessageDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Conversation not found")
    })
    public ResponseEntity<MessageDto> replyToConversation(
            @Parameter(description = "Conversation ID", example = "10")
            @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(conversationService.replyToConversation(conversationId, request));
    }

    @GetMapping
    @Operation(
            summary = "Get current user conversations",
            description = "Returns all conversations where authenticated user is a participant."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Conversations retrieved",
            content = @Content(schema = @Schema(implementation = ConversationDto.class))
    )
    public ResponseEntity<List<ConversationDto>> getMyConversations() {
        return ResponseEntity.ok(conversationService.getMyConversations());
    }

    @GetMapping("/{conversationId}/messages")
    @Operation(
            summary = "Get messages by conversation id",
            description = "Returns message history for selected conversation available to authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Messages retrieved",
                    content = @Content(schema = @Schema(implementation = MessageDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Conversation not found")
    })
    public ResponseEntity<List<MessageDto>> getConversationMessages(
            @Parameter(description = "Conversation ID", example = "10")
            @PathVariable Long conversationId) {
        return ResponseEntity.ok(conversationService.getConversationMessages(conversationId));
    }
}
