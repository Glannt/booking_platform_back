package com.dotnt.booking_travel_backend.surcharge.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface ModuleDescriptor {
    @JsonProperty("moduleCode")
    String moduleCode();

    @JsonProperty("moduleName")
    String moduleName();

    @JsonProperty("moduleCode")
    default String getModuleCode() {
        return moduleCode();
    }

    @JsonProperty("moduleName")
    default String getModuleName() {
        return moduleName();
    }
}
