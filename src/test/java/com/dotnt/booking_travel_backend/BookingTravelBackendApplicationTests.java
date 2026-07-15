package com.dotnt.booking_travel_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

@SpringBootTest
class BookingTravelBackendApplicationTests {

    static {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
    }

    @Test
    void contextLoads() {
    }

    @Test
    void createModuleDocumentation(){
        ApplicationModules modules = ApplicationModules.of(BookingTravelBackendApplication.class);
        new Documenter(modules)
                .writeDocumentation()
                .writeIndividualModulesAsPlantUml();
//        modules.forEach(System.out::println);
    }

}
