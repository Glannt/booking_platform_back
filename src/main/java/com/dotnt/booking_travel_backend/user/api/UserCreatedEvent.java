package com.dotnt.booking_travel_backend.user.api;

public record UserCreatedEvent(Long userId, String email, String role) {
}
