/**
 * Shared module containing common entities, configurations, and utilities.
 * - entity: Base MasterEntity with auditing support and soft delete
 * - utils: Helper classes and utilities for use across modules
 * - JpaAuditingConfig: Configuration for JPA auditing functionality
 */

@ApplicationModule(
        displayName = "Shared",
        allowedDependencies = {
        }
)
@NamedInterface("shared")
package com.dotnt.booking_travel_backend.shared;

import org.springframework.modulith.ApplicationModule;
import org.springframework.modulith.NamedInterface;