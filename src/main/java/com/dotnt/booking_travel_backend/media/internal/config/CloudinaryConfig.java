package com.dotnt.booking_travel_backend.media.internal.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.Map;

@Configuration
@Slf4j
@ConfigurationProperties(prefix = "cloudinary")
@Data
@ConditionalOnProperty(name = "cloudinary.cloud_name")
public class CloudinaryConfig {

    private String cloudName;
    private String apiKey;
    private String apiSecret;
    private String folder;

    @PostConstruct
    public void init() {
        log.info("Media Storage Provider initialized: Cloudinary");
    }

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = Map.of(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        );
        return new Cloudinary(config);
    }
}
