package com.dotnt.booking_travel_backend.booking.internal.service;

import com.dotnt.booking_travel_backend.booking.internal.entity.Booking;
import com.dotnt.booking_travel_backend.booking.internal.entity.BookingItem;
import com.dotnt.booking_travel_backend.tour.api.TourBookingService;
import com.dotnt.booking_travel_backend.fleet.api.FleetFacade;
import com.dotnt.booking_travel_backend.airport.api.AirportFacade;
import com.dotnt.booking_travel_backend.intercity.api.IntercityFacade;
import com.dotnt.booking_travel_backend.selfdrive.api.SelfDriveFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import org.springframework.beans.factory.annotation.Value;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingEmailBuilder {

    private final TemplateEngine templateEngine;
    private final TourBookingService tourBookingService;
    private final FleetFacade fleetFacade;
    private final AirportFacade airportFacade;
    private final IntercityFacade intercityFacade;
    private final SelfDriveFacade selfDriveFacade;

    @Value("${app.portal-url:http://localhost:3000/portal/bookings}")
    private String portalUrl;

    public String buildEmailHtml(Booking booking) {
        return buildEmailHtml(booking, false);
    }

    public String buildEmailHtml(Booking booking, boolean isAdminNotification) {
        if (booking == null || booking.getItems() == null || booking.getItems().isEmpty()) {
            return buildFallbackHtml(booking, isAdminNotification);
        }

        BookingItem item = booking.getItems().get(0);
        Context context = new Context();
        context.setVariable("booking", booking);
        context.setVariable("isAdmin", isAdminNotification);
        context.setVariable("portalUrl", portalUrl);

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
                Map<String, Object> vars = intercityFacade.getIntercityBookingEmailVariables(
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
                Map<String, Object> vars = selfDriveFacade.getSelfDriveBookingEmailVariables(
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

        return buildFallbackHtml(booking, isAdminNotification);
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

    private String resolveItemServiceName(BookingItem item) {
        String serviceName = "Dịch vụ đặt xe / du lịch";
        if (item == null || item.getDynamicDetails() == null || item.getDynamicDetails().isBlank()) {
            return serviceName;
        }

        String raw = item.getDynamicDetails().trim();
        if (raw.startsWith("{") && raw.endsWith("}")) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> map = om.readValue(raw, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                if (map.containsKey("outboundPickupAddress")) {
                    return "Đưa đón sân bay: " + map.get("outboundPickupAddress") + " ➔ " + map.get("outboundDropoffAddress");
                } else if (map.containsKey("tourName")) {
                    return "Tour: " + map.get("tourName");
                } else if (map.containsKey("pickupAddress") && map.containsKey("dropoffAddress")) {
                    return "Thuê xe liên tỉnh: " + map.get("pickupAddress") + " ➔ " + map.get("dropoffAddress");
                } else if (map.containsKey("rentalDays")) {
                    return "Thuê xe tự lái (" + map.get("rentalDays") + " ngày)";
                }
            } catch (Exception ignored) {}
        } else {
            // Parse plain-text details format: "Category ID: 2, StartDate: ..., Pickup: A, Dropoff: B"
            try {
                String pickup = null;
                String dropoff = null;
                for (String part : raw.split(",")) {
                    String trimmed = part.trim();
                    if (trimmed.toLowerCase().startsWith("pickup:")) {
                        pickup = trimmed.substring("pickup:".length()).trim();
                    } else if (trimmed.toLowerCase().startsWith("dropoff:")) {
                        dropoff = trimmed.substring("dropoff:".length()).trim();
                    }
                }
                if (pickup != null && !pickup.isEmpty() && dropoff != null && !dropoff.isEmpty()) {
                    return "Thuê xe: " + pickup + " ➔ " + dropoff;
                }
            } catch (Exception ignored) {}
        }

        return serviceName;
    }

    private String buildFallbackHtml(Booking booking, boolean isAdminNotification) {
        StringBuilder itemsHtml = new StringBuilder();
        for (BookingItem item : booking.getItems()) {
            String serviceName = resolveItemServiceName(item);
            itemsHtml.append("<tr>")
                    .append("<td style='padding: 12px 10px; border-bottom: 1px solid #e2e8f0;'><strong>").append(serviceName).append("</strong></td>")
                    .append("<td style='padding: 12px 10px; border-bottom: 1px solid #e2e8f0; text-align: center;'>").append(item.getQuantity()).append("</td>")
                    .append("<td style='padding: 12px 10px; border-bottom: 1px solid #e2e8f0; text-align: right;'>").append(String.format("%,d ₫", item.getUnitPrice() != null ? item.getUnitPrice().longValue() : 0)).append("</td>")
                    .append("<td style='padding: 12px 10px; border-bottom: 1px solid #e2e8f0; text-align: right; font-weight: 700;'>").append(String.format("%,d ₫", item.getTotalPrice() != null ? item.getTotalPrice().longValue() : 0)).append("</td>")
                    .append("</tr>");
        }

        String contactInfo = "Không có";
        if (booking.getContactChannel() != null && booking.getContactId() != null && !booking.getContactId().isEmpty()) {
            contactInfo = booking.getContactChannel().toUpperCase() + " (" + booking.getContactId() + ")";
        }

        String safeCustomerName = booking.getCustomerName() != null ? booking.getCustomerName() : "Quý khách";
        String safeBookingCode = booking.getBookingCode() != null ? booking.getBookingCode() : "N/A";
        String safePortalUrl = (portalUrl != null && !portalUrl.isBlank()) ? portalUrl : "http://localhost:3000/portal/bookings";

        String headerSubtitle = isAdminNotification
                ? "Hệ Thống Quản Trị · Thông Báo Đơn Đặt Mới"
                : "Xác Nhận Tiếp Nhận Yêu Cầu Đặt Chuyến";

        String statusBannerHtml;
        String actionButtonHtml = "";
        String footerHtml;

        if (isAdminNotification) {
            statusBannerHtml =
                "<div style='background: #fff7ed; border-left: 4px solid #ea580c; padding: 14px 16px; border-radius: 8px; font-size: 13px; color: #9a3412; margin-bottom: 18px;'>" +
                "<strong style='display: block; font-size: 14px; margin-bottom: 4px; color: #c2410c;'>CÓ ĐƠN ĐẶT CHỖ MỚI CẦN XỬ LÝ</strong>" +
                "Hệ thống vừa tiếp nhận đơn đặt mới từ khách hàng <strong>" + safeCustomerName + "</strong> (Mã đơn: <strong>" + safeBookingCode + "</strong>). " +
                "Vui lòng kiểm tra thông tin và đăng nhập trang quản trị để xem chi tiết và xác nhận đơn." +
                "</div>";

            actionButtonHtml =
                "<div style='text-align: center; margin-top: 24px; margin-bottom: 8px;'>" +
                "<a href='" + safePortalUrl + "' style='display: inline-block; background-color: #d97706; color: #ffffff; padding: 12px 28px; border-radius: 10px; font-weight: 700; text-decoration: none; font-size: 13px; box-shadow: 0 4px 6px -1px rgba(217, 119, 6, 0.2);'>Vào Trang Quản Trị Xử Lý Đơn ➔</a>" +
                "</div>";

            footerHtml =
                "<div style='background: #09090b; color: #a1a1aa; padding: 18px 20px; text-align: center; font-size: 11px; line-height: 1.6;'>" +
                "<strong style='color: #fff;'>MINH KHOA TRAVEL · HỆ THỐNG QUẢN TRỊ NỘI BỘ</strong><br>" +
                "Email tự động gửi từ hệ thống điều hành Minh Khoa Travel khi phát sinh đơn đặt mới." +
                "</div>";
        } else {
            statusBannerHtml =
                "<div style='background: #fffbeb; border-left: 4px solid #d97706; padding: 14px 16px; border-radius: 8px; font-size: 13px; color: #92400e; margin-bottom: 18px;'>" +
                "<strong style='display: block; font-size: 14px; margin-bottom: 4px; color: #78350f;'>ĐÃ TIẾP NHẬN ĐƠN ĐẶT CHỖ</strong>" +
                "Cảm ơn Quý khách <strong>" + safeCustomerName + "</strong> đã tin tưởng đặt dịch vụ tại Minh Khoa Travel. " +
                "Đơn đặt chỗ của Quý khách đã được ghi nhận thành công trên hệ thống. Nhân viên CSKH sẽ liên hệ qua điện thoại hoặc Zalo trong ít phút để xác nhận chi tiết lịch trình và hướng dẫn chu đáo." +
                "</div>";

            actionButtonHtml =
                "<div style='background: #fafafa; border: 1px solid #e4e4e7; border-radius: 8px; padding: 12px 16px; font-size: 12px; color: #52525b; margin-top: 18px; line-height: 1.6;'>" +
                "Hotline hỗ trợ 24/7: <strong style='color: #18181b;'>0987.654.321</strong> | Zalo tư vấn: <strong style='color: #18181b;'>0368.761.064</strong><br>" +
                "Nhân viên CSKH của Minh Khoa Travel sẽ chủ động liên hệ trước giờ khởi hành để gửi thông tin xe và tài xế." +
                "</div>";

            footerHtml =
                "<div style='background: #09090b; color: #a1a1aa; padding: 18px 20px; text-align: center; font-size: 11px; line-height: 1.6;'>" +
                "<strong style='color: #fff;'>CÔNG TY TNHH MINH KHOA TRAVEL</strong><br>" +
                "Nha Trang, Khánh Hòa · Hotline 24/7: 0987.654.321 · Email: tongnguyenthanhdo@gmail.com" +
                "</div>";
        }

        return "<!DOCTYPE html><html><body style='font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, \"Helvetica Neue\", Arial, sans-serif; background-color: #fafafa; padding: 24px 10px; margin: 0; color: #18181b;'>" +
                "<div style='max-width: 620px; margin: 0 auto; background: #ffffff; border-radius: 16px; overflow: hidden; border: 1px solid #e4e4e7; box-shadow: 0 10px 25px -5px rgba(0,0,0,0.06);'>" +
                "<div style='background: linear-gradient(135deg, #09090b 0%, #18181b 50%, #27272a 100%); border-top: 4px solid #d97706; color: #fff; padding: 28px 24px; text-align: center;'>" +
                "<div style='display: inline-block; background: rgba(245, 158, 11, 0.15); color: #fde68a; font-size: 11px; font-weight: 700; letter-spacing: 1px; text-transform: uppercase; padding: 4px 12px; border-radius: 9999px; margin-bottom: 8px; border: 1px solid rgba(245, 158, 11, 0.3);'>MINH KHOA TRAVEL</div>" +
                "<h2 style='margin: 0; font-size: 20px; font-weight: 800; letter-spacing: -0.5px;'>MINH KHOA TRAVEL</h2>" +
                "<p style='margin: 6px 0 0 0; font-size: 13px; color: #fde68a;'>" + headerSubtitle + "</p>" +
                "</div>" +
                "<div style='padding: 24px;'>" +
                statusBannerHtml +
                "<div style='background: #fafafa; border: 1px solid #e4e4e7; border-radius: 12px; padding: 16px; margin-bottom: 20px;'>" +
                "<div style='display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; border-bottom: 1px solid #e4e4e7; padding-bottom: 8px;'>" +
                "<span style='font-weight: 700; font-size: 14px; color: #18181b;'>Thông tin khách hàng</span>" +
                "<span style='background: #fef3c7; color: #92400e; padding: 3px 8px; border-radius: 6px; font-weight: 800; font-size: 13px; border: 1px solid #fde68a;'>" + safeBookingCode + "</span>" +
                "</div>" +
                "<p style='margin: 6px 0; font-size: 13px;'><strong>Họ và tên:</strong> " + safeCustomerName + "</p>" +
                "<p style='margin: 6px 0; font-size: 13px;'><strong>Số điện thoại:</strong> <a href='tel:" + (booking.getCustomerPhone() != null ? booking.getCustomerPhone() : "") + "' style='color: #d97706; text-decoration: none; font-weight: 600;'>" + (booking.getCustomerPhone() != null ? booking.getCustomerPhone() : "N/A") + "</a></p>" +
                "<p style='margin: 6px 0; font-size: 13px;'><strong>Email:</strong> " + (booking.getCustomerEmail() != null ? booking.getCustomerEmail() : "N/A") + "</p>" +
                "<p style='margin: 6px 0; font-size: 13px;'><strong>Kênh liên hệ:</strong> " + contactInfo + "</p>" +
                (booking.getCustomerNotes() != null && !booking.getCustomerNotes().isEmpty() ? "<p style='margin: 6px 0; font-size: 13px;'><strong>Ghi chú:</strong> <span style='color: #71717a; font-style: italic;'>" + booking.getCustomerNotes() + "</span></p>" : "") +
                "</div>" +
                "<h3 style='margin: 20px 0 10px 0; font-size: 15px; font-weight: 700; color: #18181b; border-bottom: 1px solid #f4f4f5; padding-bottom: 8px;'>Chi tiết đơn đặt</h3>" +
                "<table style='width: 100%; border-collapse: collapse; margin-top: 10px; font-size: 13px; border: 1px solid #e4e4e7; border-radius: 8px; overflow: hidden;'>" +
                "<thead><tr style='background-color: #f4f4f5; color: #52525b;'>" +
                "<th style='padding: 10px; border-bottom: 1px solid #e4e4e7; text-align: left;'>Dịch vụ</th>" +
                "<th style='padding: 10px; border-bottom: 1px solid #e4e4e7; text-align: center;'>Số lượng</th>" +
                "<th style='padding: 10px; border-bottom: 1px solid #e4e4e7; text-align: right;'>Đơn giá</th>" +
                "<th style='padding: 10px; border-bottom: 1px solid #e4e4e7; text-align: right;'>Thành tiền</th>" +
                "</tr></thead>" +
                "<tbody>" + itemsHtml.toString() + "</tbody>" +
                "<tfoot><tr style='background-color: #fefce8; font-weight: bold;'>" +
                "<td colspan='3' style='padding: 12px 10px; border-top: 2px solid #e4e4e7; text-align: right; font-weight: 700; color: #18181b;'>Tổng thanh toán:</td>" +
                "<td style='padding: 12px 10px; border-top: 2px solid #e4e4e7; text-align: right; color: #d97706; font-size: 17px; font-weight: 800;'>" + String.format("%,d ₫", booking.getTotalAmount() != null ? booking.getTotalAmount().longValue() : 0) + "</td>" +
                "</tr></tfoot>" +
                "</table>" +
                actionButtonHtml +
                "</div>" +
                footerHtml +
                "</div></body></html>";
    }
}
