package com.dotnt.booking_travel_backend.shared.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Converter(autoApply = false)
public class ComponentListConverter implements AttributeConverter<List<Component>, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Component> attribute) {
        return toJsonString(attribute);
    }

    public static String toJsonString(List<Component> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            log.error("Error serializing List<Component> to JSON", e);
            throw new IllegalArgumentException("Could not serialize List<Component>", e);
        }
    }

    @Override
    public List<Component> convertToEntityAttribute(String dbData) {
        return fromJsonString(dbData);
    }

    public static List<Component> fromJsonString(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return OBJECT_MAPPER.readValue(dbData, new TypeReference<List<Component>>() {});
        } catch (Exception e) {
            log.error("Error deserializing String to List<Component>", e);
            return new ArrayList<>();
        }
    }
}
