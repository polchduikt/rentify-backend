package com.rentify.core.config;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "application.media.upload")
public class MediaUploadProperties {

    @Positive
    private long maxFileSizeBytes = 10L * 1024 * 1024;

    private Set<String> allowedMimeTypes = new LinkedHashSet<>(Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    ));
}

