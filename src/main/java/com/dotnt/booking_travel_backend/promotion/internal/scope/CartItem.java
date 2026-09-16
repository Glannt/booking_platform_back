package com.dotnt.booking_travel_backend.promotion.internal.scope;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
    /**
     * Module name (e.g. "GLOBAL", "TOUR", "SELF_DRIVE", "INTERCITY", "INTERCITY_ROUTE", "AIRPORT", "AIRPORT_TRANSFER", "SHARED_ROUTE")
     */
    private String module;

    /**
     * Product / Route / Vehicle ID (represented as String to allow flexibility)
     */
    private String productId;

    /**
     * Item quantity or duration (days)
     */
    private Integer quantity;

    /**
     * Total amount for this item
     */
    private BigDecimal itemPrice;
}
