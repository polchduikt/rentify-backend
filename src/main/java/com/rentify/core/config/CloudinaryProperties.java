package com.rentify.core.config;

import jakarta.validation.constraints.NotBlank;
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
@ConfigurationProperties(prefix = "application.media.cloudinary")
public class CloudinaryProperties {

    @NotBlank
    private String folder = "rentify/properties";

    @Positive
    private int maxWidth = 1200;

    @Positive
    private int maxHeight = 800;
}

