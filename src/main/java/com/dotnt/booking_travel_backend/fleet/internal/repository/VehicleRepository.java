package com.dotnt.booking_travel_backend.fleet.internal.repository;

import com.dotnt.booking_travel_backend.fleet.internal.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Page<Vehicle> findByBrandContainingIgnoreCaseOrModelContainingIgnoreCase(String brand, String model, Pageable pageable);
    List<Vehicle> findByCategoryIdAndCanRentTrueAndActiveTrue(Long categoryId);

    // Dedicated strategy method 1: Category only
    @Query("SELECT v FROM Vehicle v WHERE v.category.id = :categoryId AND v.canRent = true AND v.active = true")
    List<Vehicle> findAvailableByCategoryId(@Param("categoryId") Long categoryId);

    // Dedicated strategy method 2: Category and Seat Add-on
    @Query("SELECT v FROM Vehicle v JOIN v.addOns a WHERE v.category.id = :categoryId AND a.id = :seatAddOnId AND v.canRent = true AND v.active = true")
    List<Vehicle> findAvailableByCategoryAndSeatAddOn(@Param("categoryId") Long categoryId, @Param("seatAddOnId") Long seatAddOnId);

    // Dedicated strategy method 3: Category and Fuel Add-on
    @Query("SELECT v FROM Vehicle v JOIN v.addOns a WHERE v.category.id = :categoryId AND a.id = :fuelAddOnId AND v.canRent = true AND v.active = true")
    List<Vehicle> findAvailableByCategoryAndFuelAddOn(@Param("categoryId") Long categoryId, @Param("fuelAddOnId") Long fuelAddOnId);

    // Dedicated strategy method 4: Category, Seat Add-on and Fuel Add-on
    @Query("SELECT v FROM Vehicle v WHERE v.category.id = :categoryId AND v.canRent = true AND v.active = true " +
           "AND EXISTS (SELECT 1 FROM v.addOns a1 WHERE a1.id = :seatAddOnId) " +
           "AND EXISTS (SELECT 1 FROM v.addOns a2 WHERE a2.id = :fuelAddOnId)")
    List<Vehicle> findAvailableByCategoryAndSeatAndFuelAddOns(@Param("categoryId") Long categoryId,
                                                             @Param("seatAddOnId") Long seatAddOnId,
                                                             @Param("fuelAddOnId") Long fuelAddOnId);

    // Dedicated strategy method 5: Category and arbitrary set of Add-on IDs
    @Query("SELECT v FROM Vehicle v JOIN v.addOns a WHERE v.category.id = :categoryId AND a.id IN :addOnIds AND v.canRent = true AND v.active = true " +
           "GROUP BY v HAVING COUNT(DISTINCT a.id) = :count")
    List<Vehicle> findAvailableByCategoryAndAllAddOnIds(@Param("categoryId") Long categoryId,
                                                       @Param("addOnIds") Collection<Long> addOnIds,
                                                       @Param("count") long count);
}
