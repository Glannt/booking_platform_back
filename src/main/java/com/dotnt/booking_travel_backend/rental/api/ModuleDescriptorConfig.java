package com.dotnt.booking_travel_backend.rental.api;

import com.dotnt.booking_travel_backend.surcharge.api.ModuleDescriptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("rentalModuleDescriptorConfig")
public class ModuleDescriptorConfig {

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

    @Bean
    public ModuleDescriptor selfDriveModuleDescriptor() {
        return new ModuleDescriptor() {
            @Override
            public String moduleCode() {
                return "SELF_DRIVE";
            }

            @Override
            public String moduleName() {
                return "Self-Drive Rental";
            }
        };
    }
}
