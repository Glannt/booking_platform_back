package com.dotnt.booking_travel_backend.tour.api;

import java.time.Instant;

public interface TourBookingService {
    Long createTourBookingDetail(
            Long bookingItemId,
            Long tourId,
            int guestCount,
            String pickupLocation,
            String note,
            Instant startTime,
            Instant endTime
    );

    Long createTourBookingDetail(
            Long bookingItemId,
            Long tourId,
            int guestCount,
            String pickupLocation,
            String note,
            Instant startTime,
            Instant endTime,
            Long tourPackageId,
            String packageName,
            String pricingType,
            int adultCount,
            int childCount,
            int infantCount
    );

    boolean isTour(Long serviceId);

    void updateBookingItemId(Long tourBookingDetailId, Long bookingItemId);

    String getTourName(Long tourId);

    /**
     * Whether the package is a quote request rather than a sale: it is contact-only, so it has no
     * price and books at 0đ.
     *
     * <p>Booking needs to know this at write time so it can stamp its own line item. Reading the
     * flag back later would let an admin who unticks contact-only turn every old 0đ quote into
     * revenue.
     *
     * @param tourPackageId the package being booked; a null or unknown id is not contact-only
     */
    boolean isContactOnlyPackage(Long tourPackageId);

    /**
     * What this line is worth when the server prices it: the package's active
     * {@code price_list_per_person} rows, one per passenger type, times the count of that type.
     * This is the same arithmetic the confirm page does in the browser — the difference is that
     * it reads the database instead of the request.
     *
     * <p>{@code null} means "cannot be priced at all": no package id, unknown id, or not a single
     * active row. That is a hole to report, never a price. A passenger type the booking counts but
     * the package does not price contributes 0 and logs — an infant is legitimately free, but a
     * missing adult row is lost revenue, and both must be visible rather than silently summed.
     *
     * <p>A contact-only package prices as 0 on purpose: it books, it just books as a quote.
     *
     * @param tourPackageId the package being booked
     * @param adultCount    passengers of each type on this line
     * @return the server-computed total, or {@code null} if the line cannot be priced
     */
    java.math.BigDecimal quoteTourPackage(Long tourPackageId, int adultCount, int childCount, int infantCount);

    /**
     * The deposit rule the package being booked is configured with — read from the package,
     * because the deposit belongs to what is actually sold: each variant carries its own price
     * and its own deposit rule.
     *
     * <p>{@code null} means "never configured", which is different from a policy that happens to
     * be 0: only the null books the line with no deposit at all. There is no default rate to fall
     * back on.
     *
     * @param tourPackageId the package being booked — NOT its tour id
     * @return the stored policy, or {@code null} if the package has none
     */
    com.dotnt.booking_travel_backend.shared.deposit.DepositPolicy resolveDepositPolicyForPackage(Long tourPackageId);

    java.util.Map<String, Object> getTourBookingEmailVariables(Long detailId);
}
