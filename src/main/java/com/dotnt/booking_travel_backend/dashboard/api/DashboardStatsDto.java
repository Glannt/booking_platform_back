package com.dotnt.booking_travel_backend.dashboard.api;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class DashboardStatsDto {
    private long bookingsToday;
    private long bookingsThisWeek;
    private long bookingsThisMonth;
    private long totalBookings;
    private BigDecimal totalRevenue;
    private long totalVehicleCategories;
    private long totalVehicles;
    private long totalUsers;
    private long totalTours;
}
