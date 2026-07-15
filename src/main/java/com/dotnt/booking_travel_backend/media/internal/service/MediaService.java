package com.dotnt.booking_travel_backend.media.internal.service;

import com.dotnt.booking_travel_backend.media.api.MediaDto;
import com.dotnt.booking_travel_backend.media.api.MediaResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MediaService {
    MediaResponse uploadImage(MultipartFile file, String altText, String locale);
    void deleteImage(Long id);
    MediaDto getImage(Long id);
    List<MediaDto> getImagesByIds(List<Long> ids);
}
