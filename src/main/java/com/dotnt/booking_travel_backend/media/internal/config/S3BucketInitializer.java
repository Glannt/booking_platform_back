package com.dotnt.booking_travel_backend.media.internal.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

// use a plain thread to avoid leaving an executor service open

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true")
public class S3BucketInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // Run in background so initialization doesn't block application startup
        new Thread(() -> {
            try {
                log.info("Checking S3 bucket '{}' availability...", bucketName);
                s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
                log.info("S3 Bucket '{}' already exists.", bucketName);
            } catch (NoSuchBucketException e) {
                log.info("S3 Bucket '{}' does not exist. Creating it...", bucketName);
                try {
                    s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
                    log.info("S3 Bucket '{}' created successfully.", bucketName);
                } catch (Exception ex) {
                    log.warn("Failed to create S3 bucket: {}", ex.getMessage());
                }
            } catch (SdkClientException e) {
                log.warn("S3 not reachable on startup (will not block app): {}", e.getMessage());
            } catch (Exception e) {
                log.error("Unexpected error while initializing S3 bucket", e);
            }
        }).start();
    }
}



