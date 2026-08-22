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
                            body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #faf7f2; margin: 0; padding: 30px 10px; color: #1c1917; }
                            .container { max-width: 540px; margin: 0 auto; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 25px -5px rgba(0,0,0,0.05); border: 1px solid #e7e0d6; }
                            .header { background: linear-gradient(135deg, #451a03 0%%, #78350f 50%%, #92400e 100%%); color: white; padding: 32px 24px; text-align: center; }
                            .header h1 { margin: 0; font-size: 20px; font-weight: 800; letter-spacing: -0.5px; }
                            .content { padding: 32px 24px; text-align: center; }
                            .code-container { margin: 24px 0; }
                            .code { font-size: 32px; font-weight: 800; letter-spacing: 8px; color: #92400e; background-color: #fef3c7; border: 1px solid #fde68a; padding: 14px 28px; border-radius: 12px; display: inline-block; }
                            .message { color: #57534e; font-size: 14px; line-height: 1.6; margin: 8px 0; }
                            .footer { background-color: #451a03; padding: 20px; text-align: center; font-size: 11px; color: #d6d3d1; }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <div class="header">
                                <div style="font-size: 11px; font-weight: 700; letter-spacing: 1.5px; text-transform: uppercase; margin-bottom: 8px; opacity: 0.9;">MINH KHOA TRAVEL</div>
                                <h1>Xác Thực Đặt Lại Mật Khẩu</h1>
                            </div>
                            <div class="content">
                                <p class="message">Quý khách vừa yêu cầu đặt lại mật khẩu cho tài khoản của mình.</p>
                                <p class="message">Vui lòng sử dụng mã xác thực gồm 6 chữ số dưới đây:</p>
                                <div class="code-container">
                                    <div class="code">%s</div>
                                </div>
                                <p class="message" style="font-size: 12px; color: #78716c;">Mã xác thực có hiệu lực trong vòng %d phút.</p>
                                <p class="message" style="font-size: 12px; color: #a8a29e;">Nếu Quý khách không thực hiện yêu cầu này, vui lòng bỏ qua email.</p>
                            </div>
                            <div class="footer">
                                <p style="margin: 0;">Minh Khoa Travel · Dịch vụ vận chuyển và du lịch Nha Trang</p>
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
