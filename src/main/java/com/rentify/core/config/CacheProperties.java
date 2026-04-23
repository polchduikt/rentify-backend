package com.rentify.core.config;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "cache")
public class CacheProperties {

    @Positive
    private long shortMaxAge = 60;

    @Positive
    private long shortSwr = 300;

    @Positive
    private long mediumMaxAge = 300;

    @Positive
    private long mediumSwr = 1_800;
}
