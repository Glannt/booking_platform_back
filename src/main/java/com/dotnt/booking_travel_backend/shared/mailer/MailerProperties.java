package com.dotnt.booking_travel_backend.shared.mailer;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mailer")
@Getter
@Setter
public class MailerProperties {
    private String service;
    private String from;
}
