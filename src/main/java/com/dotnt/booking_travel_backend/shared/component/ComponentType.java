package com.dotnt.booking_travel_backend.shared.component;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ComponentType {
    MARKDOWN("markdown"),
    POI("poi"),
    TEXTS("texts"),
    TEXT("text"),
    ITINERARY("itinerary"),
    SUB_GROUP("sub_group"),
    PICK_UP_MEET_UP_V2("pick_up_meet_up_v2"),
    COMPARISON("comparison");

    private final String value;

    ComponentType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ComponentType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return MARKDOWN;
        }
        for (ComponentType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return MARKDOWN;
    }
}
