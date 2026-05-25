package com.marketplace.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
    String siteName,
    Upload upload
) {
    public record Upload(
        String productsDir,
        String avatarsDir,
        DataSize maxFileSize,
        int imageMaxWidth,
        int thumbnailWidth
    ) {
    }
}
