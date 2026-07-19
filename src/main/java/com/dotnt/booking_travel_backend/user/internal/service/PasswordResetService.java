package com.dotnt.booking_travel_backend.user.internal.service;

import com.dotnt.booking_travel_backend.shared.i18n.I18nHelper;
import com.dotnt.booking_travel_backend.shared.mailer.MailService;
import com.dotnt.booking_travel_backend.user.internal.entity.PasswordResetToken;
import com.dotnt.booking_travel_backend.user.internal.entity.User;
import com.dotnt.booking_travel_backend.user.internal.repository.PasswordResetTokenRepository;
import com.dotnt.booking_travel_backend.user.internal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRY_MINUTES = 5;

    @Transactional
    public void requestReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(I18nHelper.msg("error.auth.user.notfound")));

        passwordResetTokenRepository.deleteByEmail(email);

        String code = generateCode();

        PasswordResetToken token = PasswordResetToken.builder()
                .email(email)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES))
                .used(false)
                .build();
        passwordResetTokenRepository.save(token);

        sendResetEmail(email, code);

        log.info("Password reset code sent to {}", email);
    }

    @Transactional(readOnly = true)
    public void verifyCode(String email, String code) {
        PasswordResetToken token = passwordResetTokenRepository.findByEmailAndCodeAndUsedFalse(email, code)
                .orElseThrow(() -> new IllegalArgumentException(I18nHelper.msg("error.auth.password.reset.invalid")));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(I18nHelper.msg("error.auth.password.reset.expired"));
        }

        token.setUsed(true);
        passwordResetTokenRepository.save(token);

        log.info("Password reset code verified for {}", email);
    }

    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        verifyCode(email, code);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(I18nHelper.msg("error.auth.user.notfound")));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password reset successfully for {}", email);
    }

    private String generateCode() {
        int max = (int) Math.pow(10, CODE_LENGTH);
        int code = SECURE_RANDOM.nextInt(max);
        return String.format("%0" + CODE_LENGTH + "d", code);
    }

    private void sendResetEmail(String email, String code) {
        try {
            String htmlContent = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <style>
                            body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }
                            .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                            .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; }
                            .header h1 { margin: 0; font-size: 24px; }
                            .content { padding: 30px; text-align: center; }
                            .code-container { margin: 20px 0; }
                            .code { font-size: 36px; font-weight: bold; letter-spacing: 8px; color: #667eea; background-color: #f8f9fa; padding: 15px 30px; border-radius: 8px; display: inline-block; }
                            .message { color: #666; font-size: 14px; line-height: 1.6; }
                            .footer { background-color: #f8f9fa; padding: 20px; text-align: center; font-size: 12px; color: #999; }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <div class="header">
                                <h1>Password Reset Request</h1>
                            </div>
                            <div class="content">
                                <p class="message">You requested a password reset for your account.</p>
                                <p class="message">Please use the following code to reset your password:</p>
                                <div class="code-container">
                                    <div class="code">%s</div>
                                </div>
                                <p class="message">This code will expire in %d minutes.</p>
                                <p class="message">If you did not request this, please ignore this email.</p>
                            </div>
                            <div class="footer">
                                <p>This is an automated message, please do not reply.</p>
                            </div>
                        </div>
                    </body>
                    </html>
                    """.formatted(code, CODE_EXPIRY_MINUTES);

            mailService.send(
                    email,
                    "[Booking Travel] Password Reset Code",
                    htmlContent
            );
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}", email, e);
        }
    }
}
