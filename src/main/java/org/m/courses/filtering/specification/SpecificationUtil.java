package org.m.courses.filtering.specification;

import org.springframework.data.jpa.domain.Specification;

public class SpecificationUtil {

    public static <T> Specification<T> buildEqualSpec(String fieldName, Object value) {
        return (root, cq, cb) -> cb.equal(root.get(fieldName), value);
    }
}
