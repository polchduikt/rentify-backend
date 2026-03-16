package com.rentify.core.dto.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Property rule payload")
public record PropertyRuleDto(
        @Schema(description = "Id", example = "42")
        Long id,
        @Schema(description = "Pets allowed", example = "true")
        @JsonProperty("petsAllowed") Boolean petsAllowed,
        @Schema(description = "Smoking allowed", example = "true")
        @JsonProperty("smokingAllowed") Boolean smokingAllowed,
        @Schema(description = "Parties allowed", example = "true")
        @JsonProperty("partiesAllowed") Boolean partiesAllowed,
        @Schema(description = "Additional rules", example = "Sample value")
        @JsonProperty("additionalRules") String additionalRules
) {}
