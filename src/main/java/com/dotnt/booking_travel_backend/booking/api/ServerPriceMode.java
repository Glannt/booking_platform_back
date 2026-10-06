package com.dotnt.booking_travel_backend.booking.api;

/**
 * Who wins when the figure the browser sent for a booking line disagrees with the one the server
 * priced: {@link #WARN} stores the server figure and logs both numbers so the gap can be measured
 * against real traffic; {@link #ENFORCE} additionally refuses to book a line the server cannot
 * price at all, instead of silently falling back to whatever was sent.
 *
 * <p>Configured with {@code app.booking.server-price-mode}. Spring converts the property straight
 * into this enum, so a typo fails the context at startup rather than quietly running in the wrong
 * mode — that conversion is the startup validation.
 */
public enum ServerPriceMode {
    WARN,
    ENFORCE
}
