package com.dotnt.booking_travel_backend.shared.enums;

/**
 * Danh sách các phạm vi (scope) mà 1 khuyến mãi có thể áp dụng.
 *
 * QUY ƯỚC: enum này CHỈ được thêm bởi dev khi có module nghiệp vụ mới
 * (kèm theo phải implement ScopeResolver tương ứng).
 * KHÔNG expose enum này cho admin tự thêm qua UI/API.
 */
public enum ScopeType {

    /** Áp dụng cho toàn bộ hệ thống, mọi module. scopeRefId luôn NULL. */
    GLOBAL,

    /** Toàn bộ module xe tuyến liên tỉnh (intercity route). scopeRefId = NULL -> cả module;
     *  scopeRefId = route_id -> 1 tuyến cụ thể. */
    INTERCITY_ROUTE,

    /** Module xe ghép / xe chung tuyến (shared ride). */
    SHARED_ROUTE,

    /** Module tự lái (self-drive rental). */
    SELF_DRIVE,

    /** Module tour trọn gói. */
    TOUR,

    /** Module đưa đón sân bay. */
    AIRPORT_TRANSFER;

    /**
     * true nếu scope này áp cho CẢ module (không cần scopeRefId cụ thể).
     */
    public boolean isModuleWide() {
        return this == GLOBAL;
    }
}
