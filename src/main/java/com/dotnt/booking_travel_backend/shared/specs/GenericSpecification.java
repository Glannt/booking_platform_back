package com.dotnt.booking_travel_backend.shared.specs;

import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class GenericSpecification<T> implements Specification<T> {
    private final List<SearchCriteria> criteriaList;

    public GenericSpecification() {
        this.criteriaList = new ArrayList<>();
    }

    public GenericSpecification<T> add(SearchCriteria criteria){
        this.criteriaList.add(criteria);
        return this;
    }

    public GenericSpecification<T> addIfPresent(String key, SearchOperation operation, Object object){
        if(object != null && !(object instanceof String s && s.isBlank())){
            this.criteriaList.add(new SearchCriteria(key, operation, object));
        }
        return this;
    }

    @Override
    public @Nullable Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        for (SearchCriteria c : criteriaList) {
            Path<?> path = resolvePath(root, c.getKey()); // hỗ trợ field lồng nhau vd: translations.name

            switch (c.getOperation()) {
                case EQUAL -> predicates.add(criteriaBuilder.equal(path, c.getValue()));
                case LIKE -> predicates.add(criteriaBuilder.like(criteriaBuilder.lower(path.as(String.class)),
                        "%" + c.getValue().toString().toLowerCase() + "%"));
                case GREATER_THAN -> predicates.add(criteriaBuilder.greaterThan(path.as(Comparable.class), (Comparable) c.getValue()));
                case LESS_THAN -> predicates.add(criteriaBuilder.lessThan(path.as(Comparable.class), (Comparable) c.getValue()));
                case IN -> predicates.add(path.in((Collection<?>) c.getValue()));
                case IS_NULL -> predicates.add(criteriaBuilder.isNull(path));
                case IS_NOT_NULL -> predicates.add(criteriaBuilder.isNotNull(path));
            }
        }

        query.distinct(true); // phòng trường hợp có join collection
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    // Hỗ trợ field dạng "translations.name" -> tự động join
    private Path<?> resolvePath(Root<T> root, String key) {
        if (!key.contains(".")) {
            return root.get(key);
        }
        String[] parts = key.split("\\.");
        Path<?> path = root.join(parts[0], JoinType.LEFT); // join sang quan hệ (vd: translations)
        for (int i = 1; i < parts.length; i++) {
            path = path.get(parts[i]);
        }
        return path;
    }
}
