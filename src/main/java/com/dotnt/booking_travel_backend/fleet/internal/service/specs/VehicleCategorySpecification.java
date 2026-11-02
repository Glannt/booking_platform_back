package com.dotnt.booking_travel_backend.fleet.internal.service.specs;

import com.dotnt.booking_travel_backend.fleet.internal.entity.VehicleCategory;
import com.dotnt.booking_travel_backend.fleet.internal.entity.VehicleCategoryTranslation;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class VehicleCategorySpecification {

    public static Specification<VehicleCategory> searchByCodeOrName(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();

            Join<VehicleCategory, VehicleCategoryTranslation> t = root.join("translations", JoinType.LEFT);
            String kw = "%" + search.toLowerCase() + "%";

            query.distinct(true);
            return cb.or(
                    cb.like(cb.lower(root.get("code")), kw),
                    cb.like(cb.lower(t.get("name")), kw)
            );
        };
    }
}
