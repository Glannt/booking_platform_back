package com.dotnt.booking_travel_backend.intercity.internal.entity;

public enum TripDirection {
    OUTBOUND,
    INBOUND;

    public static TripDirection fromString(String val) {
        if (val == null || val.isBlank()) return OUTBOUND;
        if ("RETURN".equalsIgnoreCase(val) || "INBOUND".equalsIgnoreCase(val)) return INBOUND;
        return OUTBOUND;
    }
}
