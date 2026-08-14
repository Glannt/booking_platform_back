package com.dotnt.booking_travel_backend.surcharge.internal.registry;

import com.dotnt.booking_travel_backend.surcharge.api.ModuleDescriptor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ModuleRegistry {

    private final Map<String, ModuleDescriptor> modules = new ConcurrentHashMap<>();

    public ModuleRegistry(List<ModuleDescriptor> descriptors) {
        if (descriptors != null) {
            for (ModuleDescriptor d : descriptors) {
                if (d.moduleCode() != null) {
                    modules.put(d.moduleCode().toUpperCase(), d);
                }
            }
        }
    }

    public boolean exists(String moduleCode) {
        if (moduleCode == null) return false;
        return modules.containsKey(moduleCode.toUpperCase());
    }

    public ModuleDescriptor get(String moduleCode) {
        if (moduleCode == null) return null;
        return modules.get(moduleCode.toUpperCase());
    }

    public List<ModuleDescriptor> getAll() {
        return new ArrayList<>(modules.values());
    }
}
