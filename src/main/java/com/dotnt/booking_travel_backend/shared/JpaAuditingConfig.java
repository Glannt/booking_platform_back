package com.dotnt.booking_travel_backend.shared;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration class to enable JPA Auditing for the modulith.
 * This enables automatic population of @CreatedDate and @LastModifiedDate annotations on entities.
 * All entities should use @EntityListeners(AuditingEntityListener.class) to enable auditing.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

}

