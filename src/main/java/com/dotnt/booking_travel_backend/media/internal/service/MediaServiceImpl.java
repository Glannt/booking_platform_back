package com.dotnt.booking_travel_backend.media.internal.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.dotnt.booking_travel_backend.media.api.MediaDto;
import com.dotnt.booking_travel_backend.media.api.MediaResponse;
import com.dotnt.booking_travel_backend.media.internal.entity.Image;
import com.dotnt.booking_travel_backend.media.internal.entity.ImageTranslation;
import com.dotnt.booking_travel_backend.media.internal.repository.ImageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class MediaServiceImpl implements MediaService {

    private final ImageRepository imageRepository;
    private final S3Client s3Client;
    private final Cloudinary cloudinary;

    @Value("${aws.s3.bucket-name:booking-travel-bucket}")
    private String bucketName;

    @Value("${aws.s3.endpoint:http://localhost:8333}")
    private String endpoint;

    @Value("${aws.s3.enabled:false}")
    private boolean s3Enabled;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${cloudinary.folder:booking_travel}")
    private String cloudinaryFolder;

    public MediaServiceImpl(ImageRepository imageRepository,
                            @Autowired(required = false) S3Client s3Client,
                            @Autowired(required = false) Cloudinary cloudinary) {
        this.imageRepository = imageRepository;
        this.s3Client = s3Client;
        this.cloudinary = cloudinary;
    }

    @Override
    @Transactional
    public MediaResponse uploadImage(MultipartFile file, String altText, String locale) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file.");
        }

        String originalFilename = file.getOriginalFilename();
        String cleanedFilename = "file";
        String extension = ".jpg";
        if (originalFilename != null) {
            String nameWithoutExtension = originalFilename;
            if (originalFilename.contains(".")) {
                nameWithoutExtension = originalFilename.substring(0, originalFilename.lastIndexOf("."));
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            cleanedFilename = nameWithoutExtension.replaceAll("\\s+", "_");
        }

        String s3Key = UUID.randomUUID().toString() + "_" + cleanedFilename + extension;
        String filePath;

        // Try reading image dimensions
        Integer width = null;
        Integer height = null;
        try {
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if (bufferedImage != null) {
                width = bufferedImage.getWidth();
                height = bufferedImage.getHeight();
            }
        } catch (Exception e) {
            log.warn("Could not determine image dimensions for {}: {}", originalFilename, e.getMessage());
        }

        // Upload priority: Cloudinary > S3 > Local
        if (cloudinary != null) {
            log.info("Using Cloudinary upload path for file: {}", originalFilename);
            filePath = uploadToCloudinary(file, s3Key);
        } else if (s3Enabled && s3Client != null) {
            log.info("Using S3 upload path for file: {}", originalFilename);
            filePath = uploadToS3(file, s3Key, originalFilename);
        } else {
            log.info("Using local upload path for file: {}", originalFilename);
            filePath = uploadToLocal(file, s3Key, originalFilename);
        }

        Image image = Image.builder()
                .fileName(s3Key)
                .filePath(filePath)
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .width(width)
                .height(height)
                .translations(new ArrayList<>())
                .build();

        if (altText != null && !altText.trim().isEmpty()) {
            ImageTranslation translation = ImageTranslation.builder()
                    .image(image)
                    .locale(locale != null ? locale : "vi")
                    .altText(altText)
                    .build();
            image.getTranslations().add(translation);
        }

        Image savedImage = imageRepository.save(image);

        return MediaResponse.builder()
                .media(mapToDto(savedImage))
                .message("Image uploaded successfully!")
                .build();
    }

    @SuppressWarnings("unchecked")
    private String uploadToCloudinary(MultipartFile file, String publicId) {
        log.info("Uploading file to Cloudinary: {}", publicId);
        try {
            String folderPath = cloudinaryFolder + "/" + publicId;
            Map<String, Object> params = ObjectUtils.asMap(
                    "public_id", folderPath,
                    "resource_type", "image"
            );
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), params);
            String url = (String) result.get("secure_url");
            log.info("Uploaded to Cloudinary: {}", url);
            return url;
        } catch (IOException e) {
            log.error("Failed to upload to Cloudinary: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload image to Cloudinary.", e);
        }
    }

    private String uploadToS3(MultipartFile file, String s3Key, String originalFilename) {
        log.info("Uploading file to S3: {} with key: {}", originalFilename, s3Key);
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return endpoint + "/" + bucketName + "/" + s3Key;
        } catch (IOException e) {
            log.error("Failed to read file input stream: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload image due to IO exception.", e);
        } catch (SdkClientException e) {
            log.error("S3 client connectivity error while uploading file {}: {}", originalFilename, e.getMessage(), e);
            throw new RuntimeException("S3_UNAVAILABLE: S3 service unreachable, please try later", e);
        } catch (S3Exception e) {
            log.error("S3 service error while uploading file {}: {}", originalFilename, e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("S3_ERROR: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    private String uploadToLocal(MultipartFile file, String s3Key, String originalFilename) {
        log.info("Uploading file locally: {} as {}", originalFilename, s3Key);
        try {
            Path dirPath = Paths.get(uploadDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            Path filePath = dirPath.resolve(s3Key);
            file.transferTo(filePath.toFile());
            return "/uploads/" + s3Key;
        } catch (IOException e) {
            log.error("Failed to save file locally: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save image locally.", e);
        }
    }

    @Override
    @Transactional
    public void deleteImage(Long id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Image not found with ID: " + id));

        if (cloudinary != null && image.getFilePath() != null && image.getFilePath().contains("cloudinary")) {
            deleteFromCloudinary(image);
        } else if (s3Enabled && s3Client != null) {
            deleteFromS3(image);
        } else {
            deleteFromLocal(image);
        }

        imageRepository.delete(image);
    }

    @SuppressWarnings("unchecked")
    private void deleteFromCloudinary(Image image) {
        log.info("Deleting image from Cloudinary: {}", image.getFilePath());
        try {
            // Extract public_id from Cloudinary URL
            // URL format: https://res.cloudinary.com/{cloud}/image/upload/v{version}/{folder}/{public_id}.{ext}
            String url = image.getFilePath();
            String publicId = url.substring(url.lastIndexOf("/") + 1);
            // Remove file extension
            if (publicId.contains(".")) {
                publicId = publicId.substring(0, publicId.lastIndexOf("."));
            }
            // Prepend folder
            if (cloudinaryFolder != null && !cloudinaryFolder.isEmpty()) {
                publicId = cloudinaryFolder + "/" + publicId;
            }
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            log.warn("Failed to delete from Cloudinary: {} (will continue)", e.getMessage());
        }
    }

    private void deleteFromS3(Image image) {
        log.info("Deleting image from S3 with key: {}", image.getFileName());
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(image.getFileName())
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (SdkClientException e) {
            log.warn("S3 client connectivity error while deleting file {}: {} (will continue)", image.getFileName(), e.getMessage());
        } catch (S3Exception e) {
            log.warn("S3 service error while deleting file {}: {} (will continue)", image.getFileName(), e.awsErrorDetails().errorMessage());
        } catch (Exception e) {
            log.warn("Unexpected error while deleting file from S3: {} (will continue)", e.getMessage());
        }
    }

    private void deleteFromLocal(Image image) {
        log.info("Deleting local file: {}", image.getFileName());
        try {
            Path filePath = Paths.get(uploadDir, image.getFileName());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Could not delete local file {}: {} (will continue)", image.getFileName(), e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MediaDto getImage(Long id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Image not found with ID: " + id));
        return mapToDto(image);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<MediaDto> getImagesByIds(java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) return java.util.List.of();
        return imageRepository.findAllById(ids).stream()
                .map(this::mapToDto)
                .toList();
    }

    private MediaDto mapToDto(Image image) {
        String altText = null;
        if (image.getTranslations() != null && !image.getTranslations().isEmpty()) {
            altText = image.getTranslations().get(0).getAltText();
        }
        return MediaDto.builder()
                .id(image.getId())
                .fileName(image.getFileName())
                .filePath(image.getFilePath())
                .fileSize(image.getFileSize())
                .mimeType(image.getMimeType())
                .width(image.getWidth())
                .height(image.getHeight())
                .altText(altText)
                .build();
    }
}
