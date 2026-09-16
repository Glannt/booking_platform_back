package com.dotnt.booking_travel_backend.selfdrive.api;

import com.dotnt.booking_travel_backend.surcharge.api.ModuleDescriptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("selfDriveModuleDescriptorConfig")
public class SelfDriveModuleDescriptorConfig {

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
