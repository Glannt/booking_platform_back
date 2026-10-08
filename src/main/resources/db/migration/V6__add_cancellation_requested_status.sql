-- V6: Allow bookings to carry the "cancellation_requested" state.
-- A customer requests a cancellation; an admin later flips the booking to "cancelled" (or back).
ALTER TABLE bookings DROP CONSTRAINT bookings_booking_status_check;

ALTER TABLE bookings
    ADD CONSTRAINT bookings_booking_status_check CHECK (((booking_status)::text = ANY ((ARRAY['pending'::character varying, 'confirmed'::character varying, 'completed'::character varying, 'cancelled'::character varying, 'cancellation_requested'::character varying])::text[])));