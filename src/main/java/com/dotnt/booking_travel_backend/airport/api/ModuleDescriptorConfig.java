package com.dotnt.booking_travel_backend.airport.api;

import com.dotnt.booking_travel_backend.surcharge.api.ModuleDescriptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("airportModuleDescriptorConfig")
public class ModuleDescriptorConfig {

    @Bean
    public ModuleDescriptor airportTransferModuleDescriptor() {
        return new ModuleDescriptor() {
            @Override
            public String moduleCode() {
                return "AIRPORT_TRANSFER";
            }

            @Override
            public String moduleName() {
                return "Airport Transfer";
            }
        };
    }
}
