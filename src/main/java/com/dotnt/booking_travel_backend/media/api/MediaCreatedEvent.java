package com.dotnt.booking_travel_backend.media.api;

public record MediaCreatedEvent(Long mediaId, String fileName, String filePath) {
}
