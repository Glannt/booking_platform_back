package com.dotnt.booking_travel_backend.shared.specs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SearchCriteria {
    private String key;          // tên field, hỗ trợ nested field vd: "translations.name"
    private SearchOperation operation;
    private Object value;
}
