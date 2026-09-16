package com.dotnt.booking_travel_backend.intercity.api;

import com.dotnt.booking_travel_backend.surcharge.api.ModuleDescriptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("intercityModuleDescriptorConfig")
public class IntercityModuleDescriptorConfig {

    @Bean
    public ModuleDescriptor intercityModuleDescriptor() {
        return new ModuleDescriptor() {
            @Override
            public String moduleCode() {
                return "INTERCITY";
            }

            @Override
            public String moduleName() {
                return "Intercity Chauffeured";
            }
        };
    }
}
