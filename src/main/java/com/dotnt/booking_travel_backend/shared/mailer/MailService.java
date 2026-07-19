package com.dotnt.booking_travel_backend.shared.mailer;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.Retryable;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;
    private final MailerProperties mailerProperties;

    @Async
    @Retryable(maxRetries = 2, delay = 2000)
    @ConcurrencyLimit(5)
    public void send(
            String to,
            String subject,
            String html
    ) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        log.info("Send thread view {}", Thread.currentThread());
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(mailerProperties.getFrom());
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html);

        mailSender.send(message);
    }
}
