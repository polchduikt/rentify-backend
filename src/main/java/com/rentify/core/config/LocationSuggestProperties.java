package com.rentify.core.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "application.locations.suggest")
public class LocationSuggestProperties {

    @Min(1)
    @Max(50)
    private int defaultLimit = 10;

    @Min(1)
    @Max(200)
    private int maxLimit = 50;
}

