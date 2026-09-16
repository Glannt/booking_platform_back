package com.dotnt.booking_travel_backend.selfdrive.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryAvailabilityDto {
    private Long vehicleCategoryId;
    private String categoryCode;
    private String categoryName;
    private Integer totalVehicles;     // Số lượng hiện có
    private Integer availableVehicles; // Số lượng có thể sử dụng
    private Integer bookedVehicles;    // Số lượng đã được đặt / chiếm chỗ
    private Boolean isAvailable;       // Còn xe để đặt không
}
