package com.dotnt.booking_travel_backend.dashboard.api;

import com.dotnt.booking_travel_backend.booking.internal.entity.Booking;
import com.dotnt.booking_travel_backend.booking.internal.repository.BookingRepository;
import com.dotnt.booking_travel_backend.fleet.internal.repository.VehicleCategoryRepository;
import com.dotnt.booking_travel_backend.fleet.internal.repository.VehicleRepository;
import com.dotnt.booking_travel_backend.tour.internal.repository.TourRepository;
import com.dotnt.booking_travel_backend.user.internal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dashboard/stats")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
public class DashboardAdminController {

    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleCategoryRepository vehicleCategoryRepository;
    private final UserRepository userRepository;
    private final TourRepository tourRepository;

    @GetMapping
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        // Fetch all active bookings
        List<Booking> bookings = bookingRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.toLocalDate().minusDays(now.getDayOfWeek().getValue() - 1).atStartOfDay();
        LocalDateTime startOfMonth = now.toLocalDate().withDayOfMonth(1).atStartOfDay();

        long bookingsToday = bookings.stream()
                .filter(b -> b.getCreatedAt() != null && (b.getCreatedAt().isAfter(startOfToday) || b.getCreatedAt().isEqual(startOfToday)))
                .count();

        long bookingsThisWeek = bookings.stream()
                .filter(b -> b.getCreatedAt() != null && (b.getCreatedAt().isAfter(startOfWeek) || b.getCreatedAt().isEqual(startOfWeek)))
                .count();

        long bookingsThisMonth = bookings.stream()
                .filter(b -> b.getCreatedAt() != null && (b.getCreatedAt().isAfter(startOfMonth) || b.getCreatedAt().isEqual(startOfMonth)))
                .count();

        long totalBookings = bookings.size();

        // Revenue excludes quote-only bookings: every line contact-only totals 0đ, and the item
        // flag is a permanent stamp so unticking contact-only later cannot resurrect an old quote.
        BigDecimal totalRevenue = bookingRepository.sumTotalAmountExcludingContactOnly();

        // Fetch counts from other repositories
        long totalVehicleCategories = vehicleCategoryRepository.count();
        long totalVehicles = vehicleRepository.count();
        long totalUsers = userRepository.count();
        long totalTours = tourRepository.count();

        DashboardStatsDto stats = DashboardStatsDto.builder()
                .bookingsToday(bookingsToday)
                .bookingsThisWeek(bookingsThisWeek)
                .bookingsThisMonth(bookingsThisMonth)
                .totalBookings(totalBookings)
                .totalRevenue(totalRevenue)
                .totalVehicleCategories(totalVehicleCategories)
                .totalVehicles(totalVehicles)
                .totalUsers(totalUsers)
                .totalTours(totalTours)
                .build();

        return ResponseEntity.ok(stats);
    }
}
