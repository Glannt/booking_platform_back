package com.dotnt.booking_travel_backend.media.api;

import com.dotnt.booking_travel_backend.shared.module.ModuleEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MediaFacade {

    private final ModuleEventPublisher moduleEventPublisher;

    public MediaDto getImage(Long id) {
        if (id == null) return null;
        return moduleEventPublisher.publishRequest("media.image.findById", id, MediaDto.class);
    }

    @SuppressWarnings("unchecked")
    public List<MediaDto> getImagesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        List<MediaDto> list = moduleEventPublisher.publishRequest("media.image.findAllByIds", ids, List.class);
        return list != null ? list : Collections.emptyList();
    }

    public String resolveThumbnailUrl(Long id) {
        if (id == null) return null;
        return moduleEventPublisher.publishRequest("media.image.resolveThumbnailUrl", id, String.class);
    }

    public void deleteImage(Long id) {
        if (id == null) return;
        moduleEventPublisher.publishRequest("media.image.delete", id, Boolean.class);
    }
}
