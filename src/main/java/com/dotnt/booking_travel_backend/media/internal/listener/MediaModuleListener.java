package com.dotnt.booking_travel_backend.media.internal.listener;

import com.dotnt.booking_travel_backend.media.api.MediaDto;
import com.dotnt.booking_travel_backend.media.internal.service.MediaService;
import com.dotnt.booking_travel_backend.shared.module.ModuleRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MediaModuleListener {

    private final MediaService mediaService;

    @EventListener
    public void handleModuleRequest(ApplicationEvent event) {
        if (!(event instanceof ModuleRequestEvent ev)) return;

        switch (ev.getRequestType()) {
            case "media.image.findById", "media.findById" -> {
                Long id = toLong(ev.getPayload());
                try {
                    MediaDto dto = id != null ? mediaService.getImage(id) : null;
                    ev.getFuture().complete(dto);
                } catch (Exception e) {
                    log.debug("Media not found for id {}: {}", id, e.getMessage());
                    ev.getFuture().complete(null);
                }
            }
            case "media.image.findAllByIds", "media.findAllById", "media.findAllByIds" -> {
                List<Long> ids = toLongList(ev.getPayload());
                try {
                    List<MediaDto> list = (ids != null && !ids.isEmpty())
                            ? mediaService.getImagesByIds(ids)
                            : Collections.emptyList();
                    ev.getFuture().complete(list);
                } catch (Exception e) {
                    log.error("Failed to load images by ids {}: {}", ids, e.getMessage());
                    ev.getFuture().complete(Collections.emptyList());
                }
            }
            case "media.image.resolveThumbnailUrl", "media.resolveThumbnailUrl", "media.image.getFilePath" -> {
                Long id = toLong(ev.getPayload());
                try {
                    MediaDto dto = id != null ? mediaService.getImage(id) : null;
                    String url = dto != null ? dto.getFilePath() : null;
                    ev.getFuture().complete(url);
                } catch (Exception e) {
                    log.debug("Thumbnail not resolved for id {}: {}", id, e.getMessage());
                    ev.getFuture().complete(null);
                }
            }
            case "media.image.delete", "media.delete" -> {
                Long id = toLong(ev.getPayload());
                if (id != null) {
                    try {
                        mediaService.deleteImage(id);
                        ev.getFuture().complete(true);
                    } catch (Exception e) {
                        log.warn("Failed to delete media id {}: {}", id, e.getMessage());
                        ev.getFuture().complete(false);
                    }
                } else {
                    ev.getFuture().complete(false);
                }
            }
            case "media.image.deleteAll", "media.deleteAll" -> {
                List<Long> ids = toLongList(ev.getPayload());
                if (ids != null) {
                    for (Long id : ids) {
                        try {
                            mediaService.deleteImage(id);
                        } catch (Exception e) {
                            log.warn("Failed to delete media id {}: {}", id, e.getMessage());
                        }
                    }
                    ev.getFuture().complete(true);
                } else {
                    ev.getFuture().complete(false);
                }
            }
            default -> log.trace("MediaModuleListener ignoring: {}", ev.getRequestType());
        }
    }

    private Long toLong(Object payload) {
        if (payload == null) return null;
        if (payload instanceof Number n) return n.longValue();
        if (payload instanceof String s) {
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private List<Long> toLongList(Object payload) {
        if (payload == null) return Collections.emptyList();
        if (payload instanceof Collection<?> coll) {
            List<Long> result = new ArrayList<>(coll.size());
            for (Object obj : coll) {
                Long val = toLong(obj);
                if (val != null) result.add(val);
            }
            return result;
        }
        if (payload instanceof Object[] arr) {
            List<Long> result = new ArrayList<>(arr.length);
            for (Object obj : arr) {
                Long val = toLong(obj);
                if (val != null) result.add(val);
            }
            return result;
        }
        Long single = toLong(payload);
        return single != null ? List.of(single) : Collections.emptyList();
    }
}
