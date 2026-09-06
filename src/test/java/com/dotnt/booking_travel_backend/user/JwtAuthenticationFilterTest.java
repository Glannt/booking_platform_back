package com.dotnt.booking_travel_backend.user;

import com.dotnt.booking_travel_backend.shared.service.PermissionService;
import com.dotnt.booking_travel_backend.user.internal.config.JwtAuthenticationFilter;
import com.dotnt.booking_travel_backend.user.internal.repository.BlacklistedTokenRepository;
import com.dotnt.booking_travel_backend.user.internal.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @Mock
    private PermissionService permissionService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_whenRoleIsNull_shouldNotThrowNpeAndDefaultToCustomerRole() throws Exception {
        String token = "valid-token-with-null-role";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(blacklistedTokenRepository.existsByToken(token)).thenReturn(false);
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn("user@example.com");
        when(jwtService.getRoleFromToken(token)).thenReturn(null);

        assertDoesNotThrow(() -> filter.doFilter(request, response, filterChain));

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("user@example.com", auth.getName());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER")));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenRoleIsValid_shouldSetRoleAndPermissions() throws Exception {
        String token = "valid-admin-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(blacklistedTokenRepository.existsByToken(token)).thenReturn(false);
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn("admin@example.com");
        when(jwtService.getRoleFromToken(token)).thenReturn("admin");
        when(permissionService.getPermissionsForRole("ADMIN")).thenReturn(java.util.List.of("TOUR_READ", "TOUR_CREATE"));

        assertDoesNotThrow(() -> filter.doFilter(request, response, filterChain));

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("admin@example.com", auth.getName());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("TOUR_READ")));
        verify(filterChain).doFilter(request, response);
    }
}
