package com.dotnt.booking_travel_backend.media.internal.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
// Credentials imports left commented; use if enabling static credentials
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
// ...existing code...

import java.net.URI;

@Configuration
@Slf4j
@ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true", matchIfMissing = true)
public class S3Config {

    @Value("${aws.s3.endpoint}")
    private String endpoint;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.access-key}")
    private String accessKey;

    @Value("${aws.s3.secret-key}")
    private String secretKey;

    // access-key/secret-key/bucket-name are read where needed (e.g. initializer or services)

    @Bean
//    @Lazy
    public S3Client s3Client() {
        log.info("Media Storage Provider initialized: S3/SeaweedFS (Region: {})", region);

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        // If SeaweedFS S3 gateway requires anonymous access keep AnonymousCredentialsProvider.
        // If credentials are required, uncomment StaticCredentialsProvider usage below.
        // AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
//                .credentialsProvider(AnonymousCredentialsProvider.create())
                .region(Region.of(region))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true) // SeaweedFS S3 requires path-style access
                        .build())
                .build();
    }
}
