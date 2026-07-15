package com.dotnt.booking_travel_backend.media.internal.controller;

import com.dotnt.booking_travel_backend.media.api.MediaDto;
import com.dotnt.booking_travel_backend.media.api.MediaResponse;
import com.dotnt.booking_travel_backend.media.internal.service.MediaService;
import com.dotnt.booking_travel_backend.shared.annotation.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PreAuthorize("hasAuthority('MEDIA_CREATE')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SuccessResponse("Media uploaded successfully!")
    public ResponseEntity<MediaResponse> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "altText", required = false) String altText,
            @RequestParam(value = "locale", required = false) String locale) {
        System.out.println("Uploading media with altText: " + altText + " and locale: " + locale);
        MediaResponse response = mediaService.uploadImage(file, altText, locale);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAuthority('MEDIA_DELETE')")
    @DeleteMapping("/{id}")
    @SuccessResponse("Media deleted successfully!")
    public ResponseEntity<Void> deleteMedia(@PathVariable Long id) {
        mediaService.deleteImage(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @SuccessResponse("Media retrieved successfully!")
    public ResponseEntity<MediaDto> getMedia(@PathVariable Long id) {
        return ResponseEntity.ok(mediaService.getImage(id));
    }

    @GetMapping("/batch")
    @SuccessResponse("Media batch retrieved successfully!")
    public ResponseEntity<java.util.List<MediaDto>> getMediaBatch(@RequestParam("ids") String ids) {
        java.util.List<Long> idList = java.util.Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .toList();
        return ResponseEntity.ok(mediaService.getImagesByIds(idList));
    }
}
