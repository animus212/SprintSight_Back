package com.example.sprintsight.configurations;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@EnableConfigurationProperties(CloudinaryProperties.class)
public class CloudinaryConfiguration {
    @Bean
    public Cloudinary cloudinary(CloudinaryProperties properties) {
        if (properties.cloudName() == null || properties.cloudName().isBlank()
                || properties.apiKey() == null || properties.apiKey().isBlank()
                || properties.apiSecret() == null || properties.apiSecret().isBlank()) {
            throw new IllegalStateException(
                    "Cloudinary is not configured. Set CLOUDINARY_CLOUD_NAME, "
                            + "CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET.");
        }

        Map<String, Object> config = ObjectUtils.asMap(
                "cloud_name", properties.cloudName(),
                "api_key",    properties.apiKey(),
                "api_secret", properties.apiSecret(),
                "secure",     true
        );
        return new Cloudinary(config);
    }
}
