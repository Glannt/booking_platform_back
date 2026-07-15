package com.dotnt.booking_travel_backend.shared.mailer;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;
    private final MailerProperties mailerProperties;

    public void send(
            String to,
            String subject,
            String html
    ) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(mailerProperties.getFrom());
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html);

        mailSender.send(message);
    }
}
