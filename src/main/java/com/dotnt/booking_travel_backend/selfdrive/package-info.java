@ApplicationModule(
        displayName = "SelfDrive",
        allowedDependencies = {
                "shared", "discount", "surcharge", "fleet"
        }
)
package com.dotnt.booking_travel_backend.selfdrive;

import org.springframework.modulith.ApplicationModule;
