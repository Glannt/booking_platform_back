package com.dotnt.booking_travel_backend.tour.api;

import com.dotnt.booking_travel_backend.surcharge.api.ModuleDescriptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("tourModuleDescriptorConfig")
public class ModuleDescriptorConfig {

    @Bean
    public ModuleDescriptor tourModuleDescriptor() {
        return new ModuleDescriptor() {
            @Override
            public String moduleCode() {
                return "TOUR";
            }

            @Override
            public String moduleName() {
                return "Tour";
            }
        };
    }
}
