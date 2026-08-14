package com.dotnt.booking_travel_backend.booking.internal.service;

import com.dotnt.booking_travel_backend.booking.internal.entity.Booking;
import com.dotnt.booking_travel_backend.booking.internal.entity.BookingItem;
import com.dotnt.booking_travel_backend.tour.api.TourBookingService;
import com.dotnt.booking_travel_backend.fleet.api.FleetFacade;
import com.dotnt.booking_travel_backend.airport.api.AirportFacade;
import com.dotnt.booking_travel_backend.rental.api.RentalFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingEmailBuilder {

    private final TemplateEngine templateEngine;
    private final TourBookingService tourBookingService;
    private final FleetFacade fleetFacade;
    private final AirportFacade airportFacade;
    private final RentalFacade rentalFacade;

    public String buildEmailHtml(Booking booking) {
        if (booking == null || booking.getItems() == null || booking.getItems().isEmpty()) {
            return buildFallbackHtml(booking);
        }

        BookingItem item = booking.getItems().get(0);
        Context context = new Context();
        context.setVariable("booking", booking);

        try {
            if (item.getAirportTransferBookingDetailId() != null || isAirportTransferItem(item)) {
                Map<String, Object> vars = null;
                if (item.getAirportTransferBookingDetailId() != null) {
                    vars = airportFacade.getAirportBookingEmailVariables(
                            item.getAirportTransferBookingDetailId(), "vi");
                }
                if (vars == null || vars.isEmpty()) {
                    vars = parseDynamicDetailsToAirportVariables(item);
                }
                if (vars != null && !vars.isEmpty()) {
                    vars.forEach(context::setVariable);
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> detailMap = (Map<String, Object>) vars.get("detail");
                    if (detailMap != null && detailMap.get("vehicleCategoryId") != null) {
                        Long vehicleCategoryId = toLong(detailMap.get("vehicleCategoryId"));
                        if (vehicleCategoryId != null) {
                            String catName = fleetFacade.getVehicleCategoryName(vehicleCategoryId, "vi");
                            context.setVariable("vehicleCategoryName", catName);
                        }
                    }
                    return templateEngine.process("email/booking-airport-transfer", context);
                }
            } else if (item.getIntercityBookingDetailId() != null) {
                Map<String, Object> vars = rentalFacade.getIntercityBookingEmailVariables(
                        item.getIntercityBookingDetailId(), "vi");
                if (vars != null && !vars.isEmpty()) {
                    vars.forEach(context::setVariable);

                    @SuppressWarnings("unchecked")
                    Map<String, Object> detailMap = (Map<String, Object>) vars.get("detail");
                    if (detailMap != null && detailMap.get("vehicleCategoryId") != null) {
                        Long vehicleCategoryId = toLong(detailMap.get("vehicleCategoryId"));
                        if (vehicleCategoryId != null) {
                            String catName = fleetFacade.getVehicleCategoryName(vehicleCategoryId, "vi");
                            context.setVariable("vehicleCategoryName", catName);
                        }
                    }
                    return templateEngine.process("email/booking-intercity", context);
                }
            } else if (item.getSelfDriveBookingDetailId() != null) {
                Map<String, Object> vars = rentalFacade.getSelfDriveBookingEmailVariables(
                        item.getSelfDriveBookingDetailId(), "vi");
                if (vars != null && !vars.isEmpty()) {
                    vars.forEach(context::setVariable);

                    @SuppressWarnings("unchecked")
                    Map<String, Object> detailMap = (Map<String, Object>) vars.get("detail");
                    if (detailMap != null && detailMap.get("vehicleCategoryId") != null) {
                        Long vehicleCategoryId = toLong(detailMap.get("vehicleCategoryId"));
                        if (vehicleCategoryId != null) {
                            String catName = fleetFacade.getVehicleCategoryName(vehicleCategoryId, "vi");
                            context.setVariable("vehicleCategoryName", catName);
                        }
                    }
                    return templateEngine.process("email/booking-self-drive", context);
                }
            } else if (item.getTourBookingDetailId() != null) {
                Map<String, Object> vars = tourBookingService.getTourBookingEmailVariables(
                        item.getTourBookingDetailId());
                if (vars != null && !vars.isEmpty()) {
                    vars.forEach(context::setVariable);

                    Long tourId = getTourIdFromItem(item);
                    if (tourId != null) {
                        String tourName = tourBookingService.getTourName(tourId);
                        context.setVariable("tourName", tourName);
                    }

                    Long vehicleCategoryId = getVehicleCategoryIdFromItem(item);
                    if (vehicleCategoryId != null) {
                        String catName = fleetFacade.getVehicleCategoryName(vehicleCategoryId, "vi");
                        context.setVariable("vehicleCategoryName", catName);
                    }

                    return templateEngine.process("email/booking-tour", context);
                }
            }
        } catch (Exception e) {
            log.error("Error processing booking email template, falling back to default styling", e);
        }

        return buildFallbackHtml(booking);
    }

    private boolean isAirportTransferItem(BookingItem item) {
        if (item.getAirportTransferBookingDetailId() != null) return true;
        if (item.getDynamicDetails() != null) {
            return item.getDynamicDetails().contains("outboundPickupAddress") || item.getDynamicDetails().contains("outboundDirection");
        }
        return false;
    }

    private Map<String, Object> parseDynamicDetailsToAirportVariables(BookingItem item) {
        if (item.getDynamicDetails() == null || item.getDynamicDetails().isBlank()) return null;
        try {
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            om.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            Map<String, Object> map = om.readValue(item.getDynamicDetails(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            if (map.containsKey("outboundPickupAddress") || map.containsKey("outboundDirection")) {
                Map<String, Object> detailMap = new java.util.HashMap<>(map);
                detailMap.put("tripType", map.get("tripType") != null ? map.get("tripType") : "ONE_WAY");
                detailMap.put("basePrice", map.get("basePrice") != null ? new java.math.BigDecimal(map.get("basePrice").toString()) : item.getUnitPrice());
                detailMap.put("totalPrice", map.get("totalPrice") != null ? new java.math.BigDecimal(map.get("totalPrice").toString()) : item.getTotalPrice());
                detailMap.put("nightSurcharge", map.get("nightSurcharge") != null ? new java.math.BigDecimal(map.get("nightSurcharge").toString()) : (map.get("surchargeAmount") != null ? new java.math.BigDecimal(map.get("surchargeAmount").toString()) : java.math.BigDecimal.ZERO));
                detailMap.put("passenger", map.get("passengerCount") != null ? Integer.valueOf(map.get("passengerCount").toString()) : 1);
                
                if (map.get("outboundPickupTime") != null) {
                    try {
                        detailMap.put("outboundPickupTime", java.time.LocalDateTime.parse(map.get("outboundPickupTime").toString()));
                    } catch (Exception ignored) {}
                }
                if (map.get("returnPickupTime") != null) {
                    try {
                        detailMap.put("returnPickupTime", java.time.LocalDateTime.parse(map.get("returnPickupTime").toString()));
                    } catch (Exception ignored) {}
                }
                return Map.of("detail", detailMap);
            }
        } catch (Exception e) {
            log.warn("Failed to parse dynamic details as airport transfer variables", e);
        }
        return null;
    }

    private Long toLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) return ((Number) obj).longValue();
        try {
            return Long.valueOf(obj.toString());
        } catch (Exception ignored) {
            return null;
        }
    }

    private Long getTourIdFromItem(BookingItem item) {
        if (item.getDynamicDetails() != null) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.Map<String, Object> ad = om.readValue(
                        item.getDynamicDetails(), 
                        new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {}
                );
                if (ad.containsKey("tourId") && ad.get("tourId") != null) {
                    return Long.valueOf(ad.get("tourId").toString());
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private Long getVehicleCategoryIdFromItem(BookingItem item) {
        if (item.getDynamicDetails() != null) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.Map<String, Object> ad = om.readValue(
                        item.getDynamicDetails(), 
                        new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {}
                );
                if (ad.containsKey("vehicleCategoryId") && ad.get("vehicleCategoryId") != null) {
                    return Long.valueOf(ad.get("vehicleCategoryId").toString());
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String buildFallbackHtml(Booking booking) {
        StringBuilder itemsHtml = new StringBuilder();
        for (BookingItem item : booking.getItems()) {
            String serviceName = "Dịch vụ đặt xe / du lịch";
            if (item.getDynamicDetails() != null) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                    Map<String, Object> map = om.readValue(item.getDynamicDetails(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                    if (map.containsKey("outboundPickupAddress")) {
                        serviceName = "Đưa đón sân bay: " + map.get("outboundPickupAddress") + " ➔ " + map.get("outboundDropoffAddress");
                    } else if (map.containsKey("tourName")) {
                        serviceName = "Tour: " + map.get("tourName");
                    } else if (map.containsKey("pickupAddress")) {
                        serviceName = "Thuê xe: " + map.get("pickupAddress") + " ➔ " + map.get("dropoffAddress");
                    }
                } catch (Exception ignored) {}
            }
            itemsHtml.append("<tr>")
                    .append("<td style='padding: 10px; border-bottom: 1px solid #eee;'><strong>").append(serviceName).append("</strong></td>")
                    .append("<td style='padding: 10px; border-bottom: 1px solid #eee; text-align: center;'>").append(item.getQuantity()).append("</td>")
                    .append("<td style='padding: 10px; border-bottom: 1px solid #eee; text-align: right;'>").append(String.format("%,d ₫", item.getUnitPrice() != null ? item.getUnitPrice().longValue() : 0)).append("</td>")
                    .append("<td style='padding: 10px; border-bottom: 1px solid #eee; text-align: right; font-weight: bold;'>").append(String.format("%,d ₫", item.getTotalPrice() != null ? item.getTotalPrice().longValue() : 0)).append("</td>")
                    .append("</tr>");
        }

        String contactInfo = "Không có";
        if (booking.getContactChannel() != null && booking.getContactId() != null) {
            contactInfo = booking.getContactChannel().toUpperCase() + " (" + booking.getContactId() + ")";
        }

        return "<!DOCTYPE html><html><body style='font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, sans-serif; background-color: #f1f5f9; padding: 20px;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background: #fff; border-radius: 12px; overflow: hidden; border: 1px solid #e2e8f0; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);'>" +
                "<div style='background: linear-gradient(135deg, #0f172a, #2563eb); color: #fff; padding: 24px; text-align: center;'>" +
                "<h2 style='margin: 0; font-size: 20px;'>MINH KHOA TRAVEL</h2>" +
                "<p style='margin: 4px 0 0 0; font-size: 13px; color: #bfdbfe;'>Xác Nhận Yêu Cầu Đặt Chuyến</p>" +
                "</div>" +
                "<div style='padding: 20px;'>" +
                "<div style='background: #fffbeb; border-left: 4px solid #f59e0b; padding: 10px 14px; border-radius: 6px; font-size: 13px; color: #92400e; margin-bottom: 16px;'>" +
                "⏳ <strong>Đang chờ xác nhận:</strong> Đơn đặt chỗ của Quý khách đã được ghi nhận. Nhân viên CSKH sẽ liên hệ trong ít phút." +
                "</div>" +
                "<p style='margin: 6px 0; font-size: 13px;'><strong>Mã đặt chỗ:</strong> <span style='color: #2563eb; font-weight: bold;'>" + booking.getBookingCode() + "</span></p>" +
                "<p style='margin: 6px 0; font-size: 13px;'><strong>Khách hàng:</strong> " + booking.getCustomerName() + "</p>" +
                "<p style='margin: 6px 0; font-size: 13px;'><strong>Số điện thoại:</strong> " + booking.getCustomerPhone() + "</p>" +
                "<p style='margin: 6px 0; font-size: 13px;'><strong>Email:</strong> " + booking.getCustomerEmail() + "</p>" +
                "<p style='margin: 6px 0; font-size: 13px;'><strong>Kênh liên hệ phụ:</strong> " + contactInfo + "</p>" +
                (booking.getCustomerNotes() != null && !booking.getCustomerNotes().isEmpty() ? "<p style='margin: 6px 0; font-size: 13px;'><strong>Ghi chú:</strong> " + booking.getCustomerNotes() + "</p>" : "") +
                "<table style='width: 100%; border-collapse: collapse; margin-top: 16px; font-size: 13px;'>" +
                "<thead><tr style='background-color: #f8fafc; color: #475569;'>" +
                "<th style='padding: 10px; border-bottom: 1px solid #e2e8f0; text-align: left;'>Dịch vụ</th>" +
                "<th style='padding: 10px; border-bottom: 1px solid #e2e8f0;'>Số lượng</th>" +
                "<th style='padding: 10px; border-bottom: 1px solid #e2e8f0; text-align: right;'>Đơn giá</th>" +
                "<th style='padding: 10px; border-bottom: 1px solid #e2e8f0; text-align: right;'>Thành tiền</th>" +
                "</tr></thead>" +
                "<tbody>" + itemsHtml.toString() + "</tbody>" +
                "<tfoot><tr>" +
                "<td colspan='3' style='padding: 12px 10px; border-top: 2px solid #e2e8f0; text-align: right; font-weight: bold;'>Tổng thanh toán:</td>" +
                "<td style='padding: 12px 10px; border-top: 2px solid #e2e8f0; text-align: right; color: #ea580c; font-size: 16px; font-weight: 800;'>" + String.format("%,d ₫", booking.getTotalAmount() != null ? booking.getTotalAmount().longValue() : 0) + "</td>" +
                "</tr></tfoot>" +
                "</table>" +
                "</div>" +
                "<div style='background: #0f172a; color: #94a3b8; padding: 16px; text-align: center; font-size: 11px;'>" +
                "<strong style='color: #fff;'>MINH KHOA TRAVEL</strong> · Hotline 24/7: 0987.654.321" +
                "</div>" +
                "</div></body></html>";
    }
}
