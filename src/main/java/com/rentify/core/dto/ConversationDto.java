package com.rentify.core.dto;

public record ConversationDto(
        Long id,
        Long propertyId,
        Long hostId,
        Long tenantId
) {}