package com.dotnt.booking_travel_backend.shared.component;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Component {

    private ComponentType type;

    private JsonNode data;
}
