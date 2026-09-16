package com.dotnt.booking_travel_backend.shared.utils;

import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
public final class EntityMergeUtils {

    private EntityMergeUtils() {}

    /**
     * Merge dto list vào existing entity list dựa theo key (3-way diff).
     * - Không đổi: entity giữ nguyên, Hibernate không sinh SQL gì.
     * - Đổi field: set lại field trên entity cũ (Hibernate dirty checking tự sinh UPDATE đúng cột đổi).
     * - DTO có nhưng existing không có: tạo entity mới và add vào existingList.
     * - Existing có nhưng DTO không có: xóa khỏi existingList (orphanRemoval tự xóa khỏi DB).
     */
    public static <E, D, K> void mergeCollection(
            List<E> existingList,
            List<D> dtoList,
            Function<E, K> existingKeyFn,
            Function<D, K> dtoKeyFn,
            BiConsumer<E, D> updateFn,
            Function<D, E> createFn
    ) {
        if (existingList == null) return;
        if (dtoList == null || dtoList.isEmpty()) {
            existingList.clear();
            return;
        }

        Map<K, E> existingByKey = existingList.stream()
                .filter(e -> existingKeyFn.apply(e) != null)
                .collect(Collectors.toMap(existingKeyFn, Function.identity(), (a, b) -> a));

        Set<K> incomingKeys = dtoList.stream()
                .map(dtoKeyFn)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 1. Xóa phần không còn trong DTO
        existingList.removeIf(e -> {
            K key = existingKeyFn.apply(e);
            return key != null && !incomingKeys.contains(key);
        });

        // 2. Update hoặc insert
        for (D dto : dtoList) {
            K key = dtoKeyFn.apply(dto);
            E existing = key != null ? existingByKey.get(key) : null;
            if (existing != null) {
                updateFn.accept(existing, dto);
            } else {
                existingList.add(createFn.apply(dto));
            }
        }
    }
}
