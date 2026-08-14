package com.dotnt.booking_travel_backend.surcharge.api;

import java.util.List;

public interface SurchargeService {
    SurchargeResult calculate(String moduleCode, PricingContext context);
    List<ModuleDescriptor> getRegisteredModules();
}
