package com.dotnt.booking_travel_backend.user.internal.config;

import com.dotnt.booking_travel_backend.shared.service.PermissionService;
import com.dotnt.booking_travel_backend.user.internal.repository.BlacklistedTokenRepository;
import com.dotnt.booking_travel_backend.user.internal.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final PermissionService permissionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        if (blacklistedTokenRepository.existsByToken(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (jwtService.validateToken(jwt)) {
            userEmail = jwtService.getEmailFromToken(jwt);
            String role = jwtService.getRoleFromToken(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));

                // Load permissions for this role and add as authorities
                try {
                    List<String> permissions = permissionService.getPermissionsForRole(role.toUpperCase());
                    for (String perm : permissions) {
                        authorities.add(new SimpleGrantedAuthority(perm));
                    }
                } catch (Exception e) {
                    log.warn("Failed to load permissions for role {}: {}", role, e.getMessage());
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userEmail,
                        null,
                        authorities
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
