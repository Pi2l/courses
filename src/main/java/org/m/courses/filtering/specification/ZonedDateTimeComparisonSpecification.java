package org.m.courses.filtering.specification;

import org.m.courses.filtering.SearchCriteria;

import javax.persistence.criteria.*;
import java.time.ZonedDateTime;

public class ZonedDateTimeComparisonSpecification<Entity> extends EqualSpecification<Entity> {

    public ZonedDateTimeComparisonSpecification(SearchCriteria criteria) {
        super(criteria);
    }

    @Override
    public Predicate toPredicate(Root<Entity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        ZonedDateTime value = ZonedDateTime.parse( criteria.getValue().toString() );
        Expression<ZonedDateTime> expression = root.get( criteria.getKey() );

        switch (criteria.getOperation()) {
            case GREATER_THEN:
                return criteriaBuilder.greaterThan( expression, value );
            case GREATER_OR_EQUAL:
                return criteriaBuilder.greaterThanOrEqualTo( expression, value );
            case LESS_THEN:
                return criteriaBuilder.lessThan( expression, value );
            case LESS_OR_EQUAL:
                return criteriaBuilder.lessThanOrEqualTo( expression, value );
            default:
                return super.toPredicate(root, query, criteriaBuilder);
        }
    }
}
