package com.dotnt.booking_travel_backend.shared.config;

import com.dotnt.booking_travel_backend.shared.service.SlidingWindowRateLimiter;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter implements Filter {

    private final SlidingWindowRateLimiter rateLimiter;

    private static final int AUTHENTICATED_WRITE_LIMIT = 30; // per minute
    private static final int ANONYMOUS_WRITE_LIMIT = 10;
    private static final int WINDOW_SECONDS = 60;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String method = httpRequest.getMethod();
        if (!"POST".equals(method) && !"PUT".equals(method) && !"DELETE".equals(method)) {
            chain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal());

        String key = authenticated ? "auth:" + auth.getName() : "anon:" + getClientIp(httpRequest);
        int limit = authenticated ? AUTHENTICATED_WRITE_LIMIT : ANONYMOUS_WRITE_LIMIT;

        if (!rateLimiter.isAllowed(key, limit, WINDOW_SECONDS)) {
            httpResponse.setStatus(429);
            httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            httpResponse.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        return (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : request.getRemoteAddr();
    }
}
