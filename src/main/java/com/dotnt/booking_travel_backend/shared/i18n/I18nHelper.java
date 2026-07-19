package com.dotnt.booking_travel_backend.shared.i18n;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Locale;

@Component
public class I18nHelper {

    private static MessageSource messageSource;

    @Autowired
    public I18nHelper(MessageSource messageSource) {
        I18nHelper.messageSource = messageSource;
    }

    public static Locale getCurrentLocale() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            String acceptLanguage = request.getHeader("Accept-Language");
            if (acceptLanguage != null && !acceptLanguage.isBlank()) {
                String lang = acceptLanguage.split(",")[0].split(";")[0].trim().toLowerCase();
                return switch (lang) {
                    case "vi" -> new Locale("vi");
                    case "ko" -> new Locale("ko");
                    case "ru" -> new Locale("ru");
                    default -> Locale.ENGLISH;
                };
            }
        }
        return Locale.ENGLISH;
    }

    public static String msg(String key, Object... args) {
        return messageSource.getMessage(key, args, getCurrentLocale());
    }
}
