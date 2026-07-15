package com.dotnt.booking_travel_backend.shared.config;

import com.dotnt.booking_travel_backend.shared.entity.IdempotencyRecord;
import com.dotnt.booking_travel_backend.shared.service.IdempotencyService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
@Slf4j
public class IdempotencyFilter implements Filter {

    private final IdempotencyService idempotencyService;

    private static final Set<String> IDEMPOTENT_METHODS = Set.of("POST", "PUT", "DELETE");
    private static final Set<String> IDEMPOTENT_PATHS = Set.of(
            "/api/v1/bookings",
            "/api/v1/airport-transfers",
            "/api/v1/admin/bookings"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String method = httpRequest.getMethod();
        String path = httpRequest.getRequestURI();

        if (!IDEMPOTENT_METHODS.contains(method) || !isIdempotentPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        String idempotencyKey = httpRequest.getHeader("X-Idempotency-Key");
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            httpResponse.getWriter().write("{\"error\":\"X-Idempotency-Key header required\"}");
            return;
        }

        var existing = idempotencyService.find(idempotencyKey);
        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();
            httpResponse.setStatus(record.getHttpStatus());
            httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            httpResponse.getWriter().write(record.getResponseBody());
            return;
        }

        // Wrap response to capture body
        CachedBodyHttpServletResponse wrappedResponse = new CachedBodyHttpServletResponse(httpResponse);
        chain.doFilter(request, wrappedResponse);

        // Store response for future replay
        String body = wrappedResponse.getCachedBody();
        if (body != null && !body.isBlank()) {
            idempotencyService.save(idempotencyKey, wrappedResponse.getStatus(), body);
        }
    }

    private boolean isIdempotentPath(String path) {
        return IDEMPOTENT_PATHS.stream().anyMatch(path::startsWith);
    }

    private static class CachedBodyHttpServletResponse extends jakarta.servlet.http.HttpServletResponseWrapper {
        private final java.io.ByteArrayOutputStream cachedBody = new java.io.ByteArrayOutputStream();

        CachedBodyHttpServletResponse(jakarta.servlet.http.HttpServletResponse response) {
            super(response);
        }

        @Override
        public java.io.PrintWriter getWriter() {
            return new java.io.PrintWriter(cachedBody);
        }

        @Override
        public jakarta.servlet.ServletOutputStream getOutputStream() {
            return new jakarta.servlet.ServletOutputStream() {
                @Override
                public void write(int b) { cachedBody.write(b); }
                @Override
                public boolean isReady() { return true; }
                @Override
                public void setWriteListener(jakarta.servlet.WriteListener listener) {}
            };
        }

        public String getCachedBody() {
            return cachedBody.toString();
        }
    }
}
