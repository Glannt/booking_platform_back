@ApplicationModule(
        displayName = "Tour",
        allowedDependencies = {"shared", "media", "surcharge", "shared :: module", "shared :: utils", "shared :: deposit"}
)
package com.dotnt.booking_travel_backend.tour;

import org.springframework.modulith.ApplicationModule;