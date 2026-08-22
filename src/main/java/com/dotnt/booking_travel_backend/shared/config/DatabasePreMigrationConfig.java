package com.dotnt.booking_travel_backend.shared.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
@Slf4j
public class DatabasePreMigrationConfig implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof DataSource dataSource) {
            try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
                boolean hasRoleId = false;
                try (ResultSet rs = stmt.executeQuery(
                        "SELECT column_name FROM information_schema.columns WHERE table_name = 'role_permissions' AND column_name = 'role_id'")) {
                    if (rs.next()) {
                        hasRoleId = true;
                    }
                } catch (Exception ignored) {}

                if (!hasRoleId) {
                    log.info("Role_permissions table is missing role_id column. Dropping stale table for Hibernate auto-recreation...");
                    stmt.execute("DROP TABLE IF EXISTS role_permissions CASCADE");
                }
            } catch (Exception e) {
                log.warn("Database pre-migration check warning: {}", e.getMessage());
            }
        }
        return bean;
    }
}
